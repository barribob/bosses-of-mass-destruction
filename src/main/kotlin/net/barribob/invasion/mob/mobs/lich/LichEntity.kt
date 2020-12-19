package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.ai.ValidatedTargetSelector
import net.barribob.invasion.mob.ai.VelocitySteering
import net.barribob.invasion.mob.ai.action.*
import net.barribob.invasion.mob.ai.goals.ActionGoal
import net.barribob.invasion.mob.ai.goals.CompositeGoal
import net.barribob.invasion.mob.ai.goals.VelocityGoal
import net.barribob.invasion.mob.ai.valid_direction.CanMoveThrough
import net.barribob.invasion.mob.ai.valid_direction.InDesiredRange
import net.barribob.invasion.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.invasion.mob.spawn.*
import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.ProjectileData
import net.barribob.invasion.mob.utils.ProjectileThrower
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.barribob.invasion.particle.ClientParticleBuilder
import net.barribob.invasion.particle.ParticleFactories
import net.barribob.invasion.particle.Particles
import net.barribob.invasion.projectile.MagicMissileProjectile
import net.barribob.invasion.projectile.comet.CometProjectile
import net.barribob.invasion.utils.ModColors
import net.barribob.invasion.utils.ModUtils
import net.barribob.invasion.utils.ModUtils.playSound
import net.barribob.invasion.utils.ModUtils.spawnParticle
import net.barribob.invasion.utils.VanillaCopies
import net.barribob.invasion.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    var velocityHistory = HistoricalData(Vec3d.ZERO)
    private val reactionDistance = 4.0
    private val summonCometStatus: Byte = 4
    private val stopAttackStatus: Byte = 5
    private val summonMissileStatus: Byte = 6
    private val summonMinionsStatus: Byte = 7
    private val teleportStatus: Byte = 8
    private val successfulTeleportStatus: Byte = 9
    private var isIdle_Client = true
    private val beginCometAttack_Client = BooleanFlag()
    private val beginMissileAttack_Client = BooleanFlag()
    private val beginMinionAttack_Client = BooleanFlag()
    private val beginTeleport_Client = BooleanFlag()
    private val endTeleport_Client = BooleanFlag()
    private val teleportParticleBuilder = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color { ModColors.TELEPORT_PURPLE }
        .age { RandomUtils.range(10, 15) }
        .brightness { Particles.FULL_BRIGHT }

    init {
        ignoreCameraFrustum = true
    }

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
        checkAttackAnimation(it, beginCometAttack_Client, "summon_fireball")
        checkAttackAnimation(it, beginMissileAttack_Client, "summon_missiles")
        checkAttackAnimation(it, beginMinionAttack_Client, "summon_minions")
        checkAttackAnimation(it, beginTeleport_Client, "teleport", "teleporting")
        checkAttackAnimation(it, endTeleport_Client, "unteleport")

        if (isIdle_Client) {
            it.controller.setAnimation(
                AnimationBuilder().addAnimation("arms_idle", true)
            )
        }

        PlayState.CONTINUE
    }

    private fun checkAttackAnimation(
        it: AnimationEvent<*>,
        booleanFlag: BooleanFlag,
        animationName: String,
        idleAnimationName: String = "arms_idle",
    ) {
        if (booleanFlag.getAndReset()) {
            it.controller.markNeedsReload()
            it.controller.setAnimation(
                AnimationBuilder()
                    .addAnimation(animationName, false)
                    .addAnimation(idleAnimationName, true)
            )
        }
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
        val cometThrowAction = buildCometAction(::canContinueAttack)
        val missileAction = buildMissileAction(::canContinueAttack)
        val minionAction = buildMinionAction(::canContinueAttack)
        val teleportAction = buildTeleportAction(::canContinueAttack)

        val attackAction = CooldownAction(teleportAction, 80)
        val onCancel = {
            world.sendEntityStatus(this, stopAttackStatus)
            attackAction.stop()
        }
        return ActionGoal(
            ::canContinueAttack,
            tickAction = attackAction,
            endAction = onCancel
        )
    }

    private fun buildTeleportAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val spawnPredicate = MobEntitySpawnPredicate(world)
        val teleportAction = IAction {
            target?.let {
                MobPlacementLogic(
                    RangedSpawnPosition({ it.pos }, 20.0, 35.0, ModRandom()),
                    { this },
                    { pos, entity -> spawnPredicate.canSpawn(pos, entity) && inLineOfSight(pos, it) },
                    { pos, entity ->
                        world.sendEntityStatus(this, teleportStatus)
                        MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                            playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 2.5f, 1.0f)
                            MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                                entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch)
                                world.sendEntityStatus(this, successfulTeleportStatus)
                                playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f)
                            }, 29))
                        }, 10, shouldCancel = { !canContinueAttack() }))
                    })
                    .tryPlacement(100)
            }
        }

        return ActionWithConstantCooldown(teleportAction, 80)
    }

    private fun inLineOfSight(
        pos: Vec3d,
        target: LivingEntity,
    ) = VanillaCopies.hasDirectLineOfSight(pos.add(newVec3d(y = standingEyeHeight.toDouble())),
        target.pos,
        world,
        this) &&
            MathUtils.facingSameDirection(target.rotationVector, MathUtils.unNormedDirection(target.pos, pos))

    private fun buildMinionAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val compoundTag = CompoundTag()
        compoundTag.putString("id", Registry.ENTITY_TYPE.getId(EntityType.PHANTOM).toString()) // Todo: want to move this into a config
        val serverWorld = world as ServerWorld
        val mobSpawner = SimpleMobSpawner(serverWorld)
        val entityProvider = CompoundTagEntityProvider(compoundTag, serverWorld, Invasions.LOGGER)
        val spawnPredicate = MobEntitySpawnPredicate(world)
        val summonCircleBeforeSpawn: (pos: Vec3d, entity: Entity) -> Unit = { pos, entity ->
            serverWorld.spawnParticle(Particles.LICH_MAGIC_CIRCLE, pos, Vec3d.ZERO)
            world.playSound(pos, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 1.5f)
            val spawnMobWithEffect = {
                mobSpawner.spawn(pos, entity)
                entity.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)
            }
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent(spawnMobWithEffect, 40, shouldCancel = { !canContinueAttack() }))
        }

        val beginSpell: () -> Unit = {
            target?.let {
                playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
                MobPlacementLogic(
                    RangedSpawnPosition({ it.pos }, 4.0, 8.0, ModRandom()),
                    entityProvider,
                    spawnPredicate,
                    summonCircleBeforeSpawn
                ).tryPlacement(30)
            }
        }

        val summonMobsAction = IAction {
            world.sendEntityStatus(this, summonMinionsStatus)
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent(beginSpell, 40, shouldCancel = { !canContinueAttack() }))
        }

        return ActionWithConstantCooldown(summonMobsAction, 80)
    }

    private fun buildMissileAction(canContinueAttack: () -> Boolean): ActionWithConstantCooldown {
        val cometThrower = { offset: Vec3d ->
            ProjectileThrower {
                val projectile = MagicMissileProjectile(this, world)
                projectile.setPos(getCameraPosVec(0f).add(offset))
                world.spawnEntity(projectile)
                ProjectileData(projectile, 1.6f, 0f)
            }
        }

        val throwMissilesAction = {
            target?.let {
                val target = it.boundingBox.center
                for (offset in getMissileLaunchOffsets()) {
                    cometThrower(offset).throwProjectile(target.add(offset.planeProject(VecUtils.yAxis)))
                }
            }
            playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)
        }

        val readyMissilesAction = {
            MaelstromMod.serverEventScheduler.addEvent(TimedEvent(throwMissilesAction,
                46, shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonMissileStatus)
            playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
        }

        return ActionWithConstantCooldown(readyMissilesAction, 80)
    }

    private fun buildCometAction(canContinueAttack: () -> Boolean): ActionWithConstantCooldown {
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

        return ActionWithConstantCooldown(readyCometAction, 80)
    }

    private fun buildAttackMovement(): VelocityGoal {
        val tooCloseDistance = 20.0
        val tooFarDistance = 35.0
        val tooCloseToTarget: (Vec3d) -> Boolean = getWithinDistancePredicate(tooCloseDistance) { this.target!!.pos }
        val tooFarFromTarget: (Vec3d) -> Boolean =
            { !getWithinDistancePredicate(tooFarDistance) { this.target!!.pos }(it) }
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
            isIdle_Client = false
            beginCometAttack_Client.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                ParticleFactories.cometTrail().build(getCometLaunchPosition())
            }, 15, 45, ::shouldCancelAttackAnimation))
        }
        if (status == summonMissileStatus) {
            isIdle_Client = false
            beginMissileAttack_Client.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                for (offset in getMissileLaunchOffsets()) {
                    ParticleFactories.soulFlame().age { 2 }.build(getCameraPosVec(0f).add(offset))
                }
            }, 16, 30, ::shouldCancelAttackAnimation))
        }
        if (status == summonMinionsStatus) {
            isIdle_Client = false
            beginMinionAttack_Client.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                ParticleFactories.soulFlame()
                    .color { ModColors.WHITE }
                    .velocity(VecUtils.yAxis.multiply(RandomUtils.double(0.2) + 0.2))
                    .build(getCameraPosVec(0f)
                        .add(VecUtils.yAxis.multiply(1.0))
                        .add(RandomUtils.randVec()
                            .planeProject(VecUtils.yAxis)
                            .normalize()
                            .multiply(getRandom().nextGaussian())))
            }, 10, 40, ::shouldCancelAttackAnimation))
        }
        if (status == teleportStatus) {
            isIdle_Client = false
            beginTeleport_Client.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent(::spawnTeleportParticles,
                15,
                10,
                ::shouldCancelAttackAnimation))
        }
        if (status == successfulTeleportStatus) {
            endTeleport_Client.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent(::spawnTeleportParticles,
                1,
                10,
                ::shouldCancelAttackAnimation))
        }
        if (status == stopAttackStatus) {
            isIdle_Client = true
        }
        super.handleStatus(status)
    }

    private fun spawnTeleportParticles() {
        teleportParticleBuilder.build(getCameraPosVec(0f)
            .add(RandomUtils.randVec()
                .multiply(3.0)))
    }

    private fun getCometLaunchPosition() = pos.add(VecUtils.yAxis.multiply(4.0))
    private fun getMissileLaunchOffsets(): List<Vec3d> {
        return listOf(
            MathUtils.axisOffset(rotationVector, VecUtils.yAxis.add(VecUtils.zAxis.multiply(2.0))),
            MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(1.5).add(VecUtils.zAxis)),
            MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(2.0)),
            MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(1.5).add(VecUtils.zAxis.negateServer())),
            MathUtils.axisOffset(rotationVector, VecUtils.yAxis.add(VecUtils.zAxis.negateServer().multiply(2.0)))
        )
    }

    private fun canContinueAttack() = isAlive && target != null
    private fun shouldCancelAttackAnimation() = !isAlive || isIdle_Client

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?,
    ) {
        this.moveControl
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this)
    }

    override fun isClimbing(): Boolean = false
}