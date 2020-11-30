package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.Particles
import net.barribob.invasion.mob.ai.ValidatedTargetSelector
import net.barribob.invasion.mob.ai.VelocitySteering
import net.barribob.invasion.mob.ai.action.ActionWithConstantCooldown
import net.barribob.invasion.mob.ai.action.CooldownAction
import net.barribob.invasion.mob.ai.action.SnowballThrowAction
import net.barribob.invasion.mob.ai.goals.ActionGoal
import net.barribob.invasion.mob.ai.goals.CompositeGoal
import net.barribob.invasion.mob.ai.goals.VelocityGoal
import net.barribob.invasion.mob.ai.valid_direction.CanMoveThrough
import net.barribob.invasion.mob.ai.valid_direction.InDesiredRange
import net.barribob.invasion.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.barribob.invasion.utils.VanillaCopies
import net.barribob.invasion.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.addVelocity
import net.barribob.maelstrom.static_utilities.newVec3d
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    var velocityHistory = HistoricalData(Vec3d.ZERO)
    private val reactionDistance = 4.0

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "skull_float", 0f, createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this, "arms_idle", 0f, createIdlePredicate("arms_idle")))
        data.addAnimationController(AnimationController(this, "book_idle", 0f, createIdlePredicate("book_idle")))
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
        val snowballThrowAction = CooldownAction(ActionWithConstantCooldown(SnowballThrowAction(this), 20), 60)
        return ActionGoal(
            { target != null },
            tickAction = snowballThrowAction,
            endAction = snowballThrowAction
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

    private fun createIdlePredicate(animationName: String): AnimationPredicate<LichEntity> = AnimationPredicate {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation(animationName, true)
        )
        PlayState.CONTINUE
    }

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