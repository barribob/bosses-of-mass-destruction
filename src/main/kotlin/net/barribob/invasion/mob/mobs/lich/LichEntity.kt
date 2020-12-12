package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.mob.ai.ValidatedTargetSelector
import net.barribob.invasion.mob.ai.VelocitySteering
import net.barribob.invasion.mob.ai.action.ActionWithConstantCooldown
import net.barribob.invasion.mob.ai.action.CooldownAction
import net.barribob.invasion.mob.ai.action.ThrowProjectileAction
import net.barribob.invasion.mob.ai.goals.ActionGoal
import net.barribob.invasion.mob.ai.goals.CompositeGoal
import net.barribob.invasion.mob.ai.goals.VelocityGoal
import net.barribob.invasion.mob.ai.valid_direction.CanMoveThrough
import net.barribob.invasion.mob.ai.valid_direction.InDesiredRange
import net.barribob.invasion.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.ProjectileData
import net.barribob.invasion.mob.utils.ProjectileThrower
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.barribob.invasion.particle.ParticleFactories
import net.barribob.invasion.particle.Particles
import net.barribob.invasion.projectile.comet.CometProjectile
import net.barribob.invasion.utils.ModUtils
import net.barribob.invasion.utils.VanillaCopies
import net.barribob.invasion.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

/**
 * For the most part, our quick and dirty tracer code to create a nice attack that the player can react to was a success
 *
 * Few problems
 * - The attack range is way off - we're going to use minecraft standards for now
 * - The movement ai doesn't seem to work in all cases - Figure this out late - hopefully teleportation can cover this
 *
 * - How to not make this class awful to read and to find things in. It's already near 300 lines and counting
 */
class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    var velocityHistory = HistoricalData(Vec3d.ZERO)
    private val reactionDistance = 4.0
    private val summonCometStatus: Byte = 16
    private val stopAttackStatus: Byte = 17
    private var cometAttackFlag = false
    private val attackFlag = BooleanFlag()

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "attack", 0f, attack))
        data.addAnimationController(AnimationController(this,
            "skull_float",
            0f,
            ModUtils.createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, ModUtils.createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this,
            "book_idle",
            0f,
            ModUtils.createIdlePredicate("book_idle")))
    }

    private val attack = AnimationPredicate<LichEntity> {
        if (attackFlag.getAndReset()) {
            it.controller.markNeedsReload()
            it.controller.setAnimation(
                AnimationBuilder().addAnimation("summon_fireball", false).addAnimation("arms_idle", true)
            )
        }

        if (!cometAttackFlag) {
            it.controller.setAnimation(
                AnimationBuilder().addAnimation("arms_idle", true)
            )
        }

        PlayState.CONTINUE
    }

    override fun initGoals() {
        goalSelector.add(1, SwimGoal(this))
        goalSelector.add(2, buildDefendGoal())
        goalSelector.add(3, CompositeGoal(listOf(buildAttackGoal(), buildAttackMovement())))
        goalSelector.add(4, buildWanderGoal())

        targetSelector.add(
            2, FollowTargetGoal(
                this,
                LivingEntity::class.java, true
            )
        )
    }

    private fun buildAttackGoal(): ActionGoal {
        val canContinueAttack = { isAlive && target != null }

        val cometThrower = ProjectileThrower {
            val projectile = CometProjectile(this, world)
            projectile.setPos(getCometLaunchPosition())
            world.spawnEntity(projectile)
            ProjectileData(projectile, 1.6f, 0f)
        }

        val throwCometAction = {
            ThrowProjectileAction(this, cometThrower).perform()
            playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)
        }

        val readyCometAction = {
            MaelstromMod.serverEventScheduler.addEvent(TimedEvent(throwCometAction,
                60,
                shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonCometStatus)
            playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
        }

        val cometThrowAction = ActionWithConstantCooldown(readyCometAction, 80)

        val attackAction = CooldownAction(cometThrowAction, 80)
        val onCancel = {
            world.sendEntityStatus(this, stopAttackStatus)
            attackAction.stop()
        }
        return ActionGoal(
            canContinueAttack,
            tickAction = attackAction,
            endAction = onCancel
        )
    }

    private fun buildAttackMovement(): VelocityGoal {
        val tooCloseDistance = 20.0
        val tooFarDistance = 35.0
        val tooCloseToTarget: (Vec3d) -> Boolean = getWithinDistancePredicate(tooCloseDistance) { this.target!!.pos }
        val tooFarFromTarget: (Vec3d) -> Boolean = { !getWithinDistancePredicate(tooFarDistance) { this.target!!.pos }(it) }
        val movingToTarget: (Vec3d) -> Boolean = { MathUtils.movingTowards(this.target!!.pos, pos, it) }

        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(this, reactionDistance),
                InDesiredRange(tooCloseToTarget, tooFarFromTarget, movingToTarget)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            this,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveWhileAttacking,
            createSteering(),
            targetSelector
        )
    }

    private fun buildDefendGoal() = ActionGoal(
        ::shouldDefend,
        startAction = {
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent(
                    { isInvulnerable = true },
                    10,
                    shouldCancel = { !shouldDefend() })
            )
        },
        endAction = {
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent(
                    { isInvulnerable = false },
                    20,
                    shouldCancel = ::shouldDefend
                )
            )
        }
    )

    private fun shouldDefend(): Boolean = target != null && target?.isUsingItem == true

    private fun buildWanderGoal(): VelocityGoal {
        val idleWanderDistance = 25.0
        val tooFarFromTarget: (Vec3d) -> Boolean = getWithinDistancePredicate(idleWanderDistance) { idlePosition }
        val movingTowardsIdleCenter: (Vec3d) -> Boolean = { MathUtils.movingTowards(idlePosition, pos, it) }
        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(this, reactionDistance),
                InDesiredRange({ false }, tooFarFromTarget, movingTowardsIdleCenter)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            this,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveTowards,
            createSteering(),
            targetSelector
        )
    }

    private fun createSteering() = VelocitySteering(this, 4.0, 120.0)

    private fun getWithinDistancePredicate(distance: Double, targetPos: () -> Vec3d): (Vec3d) -> Boolean = {
        val target = pos.add(it.multiply(reactionDistance))
        MathUtils.withinDistance(target, targetPos(), distance)
    }

    private fun moveTowards(velocity: Vec3d) {
        addVelocity(velocity)

        val lookTarget = pos.add(newVec3d(y = standingEyeHeight.toDouble())).add(velocity)
        lookControl.lookAt(lookTarget)
        lookAtTarget(lookTarget, bodyYawSpeed.toFloat(), lookPitchSpeed.toFloat())
    }

    private fun moveWhileAttacking(velocity: Vec3d) {
        addVelocity(velocity)

        if (target != null) {
            lookControl.lookAt(target!!.pos)
            lookAtTarget(target!!.pos, bodyYawSpeed.toFloat(), lookPitchSpeed.toFloat())
        }
    }

    override fun clientTick() {
        velocityHistory.set(velocity)
    }

    override fun serverTick() {
        if (isInvulnerable) {
            (world as ServerWorld).spawnParticles(Particles.SKELETON, pos.x, pos.y + 5, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun handleStatus(status: Byte) {
        if (status == summonCometStatus) {
            cometAttackFlag = true
            attackFlag.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                ParticleFactories.COMET_TRAIL.build(getCometLaunchPosition())
            }, 15, 45, { !isAlive || !cometAttackFlag }))
        }
        if (status == stopAttackStatus) {
            cometAttackFlag = false
        }
        super.handleStatus(status)
    }

    private fun getCometLaunchPosition() = pos.add(VecUtils.yAxis.multiply(4.0))

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?
    ) {
        this.moveControl
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this)
    }

    override fun isClimbing(): Boolean = false
}