package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.ai.BossVisibilityCache
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
import net.barribob.invasion.utils.AnimationUtils
import net.barribob.invasion.utils.ModColors
import net.barribob.invasion.utils.ModUtils.playSound
import net.barribob.invasion.utils.ModUtils.spawnParticle
import net.barribob.invasion.utils.VanillaCopies
import net.barribob.invasion.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.general.random.WeightedRandom
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.damage.DamageSource
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
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    val velocityHistory = HistoricalData(Vec3d.ZERO)
    private val positionalHistory = HistoricalData(Vec3d.ZERO, 10)
    private val damageHistory = HistoricalData(0, 3)
    private val moveHistory = HistoricalData(IActionWithCooldown { 0 }, 3)
    private val reactionDistance = 4.0
    private val summonCometStatus: Byte = 4
    private val stopAttackStatus: Byte = 5
    private val summonMissileStatus: Byte = 6
    private val summonMinionsStatus: Byte = 7
    private val teleportStatus: Byte = 8
    private val successfulTeleportStatus: Byte = 9
    private val fireballRageStatus: Byte = 10
    private val missileRageStatus: Byte = 11
    private var doIdleAnimation = true
    private val doCometAttackAnimation = BooleanFlag()
    private val doMissileAttackAnimation = BooleanFlag()
    private val doMinionAttackAnimation = BooleanFlag()
    private val doTeleportAnimation = BooleanFlag()
    private val doEndTeleportAnimation = BooleanFlag()
    private val doRageAnimation = BooleanFlag()
    private var collides = true

    private val cometThrowDelay = 60
    private val cometParticleSummonDelay = 15
    private val cometThrowCooldown = 80

    private val missileThrowDelay = 46
    private val missileThrowCooldown = 80
    private val missileParticleSummonDelay = 16

    private val minionRuneToMinionSpawnDelay = 40
    private val minionSummonDelay = 40
    private val minionSummonParticleDelay = 10
    private val minionSummonCooldown = 80

    private val numMobs = 9
    private val initialSpawnTimeCooldown = 40
    private val initialBetweenSpawnDelay = 40
    private val spawnDelayDecrease = 3
    private val delayTimes: List<Int>
    private val totalMoveTime: Int

    private val teleportCooldown = 80
    private val teleportStartSoundDelay = 10
    private val teleportDelay = 40
    private val beginTeleportParticleDelay = 15
    private val teleportParticleDuration = 10

    private val numCometsDuringRage = 6
    private val initialRageCometDelay = 60
    private val delayBetweenRageComets = 30
    private val rageCometsMoveDuration: Int

    private val ragedMissileVolleyInitialDelay = 60
    private val ragedMissileVolleyBetweenVolleyDelay = 30
    private val ragedMissileParticleDelay = 30

    private val tooFarFromTargetDistance = 35.0
    private val tooCloseToTargetDistance = 20.0
    private val idleWanderDistance = 25.0

    private val visibilityCache = BossVisibilityCache(this)

    private val summonMissileParticleBuilder = ParticleFactories.soulFlame().age { 2 }
    private val teleportParticleBuilder = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color { ModColors.TELEPORT_PURPLE }
        .age { RandomUtils.range(10, 15) }
        .brightness { Particles.FULL_BRIGHT }
    private val summonCometParticleBuilder = ParticleFactories.cometTrail()
    private val flameRingFactory = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.WHITE.multiply(0.5)) }
        .brightness { Particles.FULL_BRIGHT }
        .age { RandomUtils.range(0, 7) }
    private val minionSummonParticleBuilder = ParticleFactories.soulFlame()
        .color { ModColors.WHITE }
        .velocity(VecUtils.yAxis.multiply(RandomUtils.double(0.2) + 0.2))

    private val missileThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = MagicMissileProjectile(this, world)
            projectile.setPos(eyePos().add(offset))
            world.spawnEntity(projectile)
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    private val cometThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = CometProjectile(this, world)
            projectile.setPos(eyePos().add(offset))
            world.spawnEntity(projectile)
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    private val cometThrowAction = buildCometAction(::canContinueAttack)
    private val missileAction = buildMissileAction(::canContinueAttack)
    private val minionAction = buildMinionAction(::canContinueAttack)
    private val teleportAction = buildTeleportAction(::canContinueAttack) { target }
    private val cometRageAction = buildCometRageAction(::canContinueAttack)
    private val volleyRageAction = buildVolleyRageAction(::canContinueAttack)
    private val minionRageAction = buildMinionRageAction(::canContinueAttack)

    private fun playCometLaunchSound() = playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)
    private fun playCometPrepareSound() = playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
    private fun playVolleyShootSound() = playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)
    private fun playVolleyPrepareSound() = playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
    private fun playBeginTeleportSound() = playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 2.5f, 1.0f)
    private fun playTeleportSound() = playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f)
    private fun playPrepareSummonMinionSound() = playSound(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, 2.0f, 1.0f)
    private fun playMinionRuneSound() =
        world.playSound(pos, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 1.5f)

    private fun playMinionSummonSound(entity: Entity) = entity.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f)

    init {
        ignoreCameraFrustum = true
        delayTimes = (0 until numMobs)
            .map { MathUtils.consecutiveSum(0, it) }
            .mapIndexed { index, i -> initialSpawnTimeCooldown + (index * initialBetweenSpawnDelay) - (i * spawnDelayDecrease) }
        totalMoveTime = delayTimes.last() + minionRuneToMinionSpawnDelay
        rageCometsMoveDuration = initialRageCometDelay + (numCometsDuringRage * delayBetweenRageComets)

        if (!world.isClient) {
            goalSelector.add(1, SwimGoal(this))
            goalSelector.add(3, CompositeGoal(listOf(buildAttackGoal(), buildAttackMovement())))
            goalSelector.add(4, buildWanderGoal())

            targetSelector.add(
                2, FollowTargetGoal(
                    this,
                    LivingEntity::class.java, true
                )
            )
        }
    }

    override fun getVisibilityCache() = visibilityCache

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "attack", 0f, attack))
        data.addAnimationController(AnimationController(this,
            "skull_float",
            0f,
            AnimationUtils.createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, AnimationUtils.createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this,
            "book_idle",
            0f,
            AnimationUtils.createIdlePredicate("book_idle")))
    }

    private val attack = AnimationPredicate<LichEntity> {
        AnimationUtils.checkAttackAnimation(it, doCometAttackAnimation, "summon_fireball", "arms_idle")
        AnimationUtils.checkAttackAnimation(it, doMissileAttackAnimation, "summon_missiles", "arms_idle")
        AnimationUtils.checkAttackAnimation(it, doMinionAttackAnimation, "summon_minions", "arms_idle")
        AnimationUtils.checkAttackAnimation(it, doTeleportAnimation, "teleport", "teleporting")
        AnimationUtils.checkAttackAnimation(it, doEndTeleportAnimation, "unteleport", "arms_idle")
        AnimationUtils.checkAttackAnimation(it, doRageAnimation, "rage_mode", "arms_idle")

        if (doIdleAnimation) {
            it.controller.setAnimation(
                AnimationBuilder().addAnimation("arms_idle", true)
            )
        }

        PlayState.CONTINUE
    }

    private fun buildAttackGoal(): ActionGoal {

        val attackAction = CooldownAction({
            target?.let {
                val random = WeightedRandom<IActionWithCooldown>()
                val distanceTraveled = positionalHistory.getAll().zipWithNext()
                    .fold(0.0) { acc, pair -> acc + pair.first.distanceTo(pair.second) }
                val damage = damageHistory.getAll()
                val hasBeenRapidlyDamaged = damage.size > 2 && damage.last() - damage.first() < 60
                val teleportWeight = 0.0 +
                        (if (inLineOfSight(pos, it)) 0.0 else 4.0) +
                        (if (distanceTraveled > 0.25) 0.0 else 8.0) +
                        (if (distanceTo(target) < 6.0) 8.0 else 0.0) +
                        (if (hasBeenRapidlyDamaged) 8.0 else 0.0)
                val minionWeight = if (moveHistory.getAll().contains(minionAction)) 0.0 else 2.0

                random.addAll(
                    listOf(
                        Pair(1.0, cometThrowAction),
                        Pair(1.0, missileAction),
                        Pair(minionWeight, minionAction),
                        Pair(teleportWeight, teleportAction)
                    )
                )
                val nextMove = random.next()
                moveHistory.set(nextMove)
                nextMove.perform()
            } ?: 80
        }, 80)
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

    private fun buildVolleyRageAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val rageMissileVolleys = getRageMissileVolleys().size
        val throwMissilesAction = IAction {
            target?.let {
                world.sendEntityStatus(this, missileRageStatus)
                playVolleyPrepareSound()
                for (i in 0 until rageMissileVolleys) {
                    MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                        val target = it.boundingBox.center
                        for (offset in getRageMissileVolleys()[i]) {
                            missileThrower(offset).throwProjectile(target.add(offset))
                        }
                        playVolleyShootSound()
                    },
                        ragedMissileVolleyInitialDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                        shouldCancel = { !canContinueAttack() }))
                }
            }
        }

        return ActionWithConstantCooldown(throwMissilesAction,
            ragedMissileVolleyInitialDelay + (rageMissileVolleys * ragedMissileVolleyBetweenVolleyDelay))
    }

    private fun buildCometRageAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val readyCometsAction = IAction {
            target?.let {
                for ((i, offset) in getRageCometOffsets().withIndex()) {
                    MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                        val target = it.boundingBox.center
                        cometThrower(offset).throwProjectile(target)
                        playCometLaunchSound()
                    }, initialRageCometDelay + (i * delayBetweenRageComets), shouldCancel = { !canContinueAttack() }))
                }
                world.sendEntityStatus(this, fireballRageStatus)
                playCometPrepareSound()
            }
        }

        return ActionWithConstantCooldown(readyCometsAction, rageCometsMoveDuration)
    }

    private fun buildTeleportAction(
        canContinueAttack: () -> Boolean,
        target: () -> LivingEntity?,
    ): IActionWithCooldown {
        val spawnPredicate = MobEntitySpawnPredicate(world)
        val teleportAction = IAction {
            target()?.let {
                MobPlacementLogic(
                    RangedSpawnPosition({ it.pos }, tooCloseToTargetDistance, tooFarFromTargetDistance, ModRandom()),
                    { this },
                    { pos, entity -> spawnPredicate.canSpawn(pos, entity) && inLineOfSight(pos, it) },
                    { pos, entity ->
                        world.sendEntityStatus(this, teleportStatus)
                        MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                            playBeginTeleportSound()
                            collides = false
                            MaelstromMod.serverEventScheduler.addEvent(TimedEvent({
                                entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch)
                                world.sendEntityStatus(this, successfulTeleportStatus)
                                playTeleportSound()
                                collides = true
                            }, teleportDelay - teleportStartSoundDelay))
                        }, teleportStartSoundDelay, shouldCancel = { !canContinueAttack() }))
                    })
                    .tryPlacement(100)
            }
        }

        return ActionWithConstantCooldown(teleportAction, teleportCooldown)
    }

    private fun inLineOfSight(pos: Vec3d, target: LivingEntity) = VanillaCopies.hasDirectLineOfSight(
        pos.add(newVec3d(y = standingEyeHeight.toDouble())),
        target.pos,
        world,
        this) &&
            MathUtils.facingSameDirection(target.rotationVector, MathUtils.unNormedDirection(target.pos, pos))

    private fun buildMinionRageAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val summonMobsAction = IAction {
            world.sendEntityStatus(this, summonMinionsStatus)
            for (delayTime in delayTimes) {
                MaelstromMod.serverEventScheduler.addEvent(TimedEvent({ beginSummonSingleMob(canContinueAttack) },
                    delayTime, shouldCancel = { !canContinueAttack() }))
            }
        }

        return ActionWithConstantCooldown(summonMobsAction, totalMoveTime)
    }

    private fun buildMinionAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val summonMobsAction = IAction {
            world.sendEntityStatus(this, summonMinionsStatus)
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent({ beginSummonSingleMob(canContinueAttack) },
                    minionSummonDelay,
                    shouldCancel = { !canContinueAttack() }))
        }

        return ActionWithConstantCooldown(summonMobsAction, minionSummonCooldown)
    }

    private fun beginSummonSingleMob(canContinueAttack: () -> Boolean) {
        val compoundTag = CompoundTag()
        compoundTag.putString("id",
            Registry.ENTITY_TYPE.getId(EntityType.PHANTOM).toString()) // Todo: want to move this into a config
        val serverWorld = world as ServerWorld
        val mobSpawner = SimpleMobSpawner(serverWorld)
        val entityProvider = CompoundTagEntityProvider(compoundTag, serverWorld, Invasions.LOGGER)
        val spawnPredicate = MobEntitySpawnPredicate(world)
        val summonCircleBeforeSpawn: (pos: Vec3d, entity: Entity) -> Unit = { pos, entity ->
            serverWorld.spawnParticle(Particles.LICH_MAGIC_CIRCLE, pos, Vec3d.ZERO)
            playMinionRuneSound()
            val spawnMobWithEffect = {
                mobSpawner.spawn(pos, entity)
                playMinionSummonSound(entity)
            }
            MaelstromMod.serverEventScheduler.addEvent(
                TimedEvent(spawnMobWithEffect, minionRuneToMinionSpawnDelay, shouldCancel = { !canContinueAttack() }))
        }

        target?.let {
            playPrepareSummonMinionSound()
            MobPlacementLogic(
                RangedSpawnPosition({ it.pos }, 4.0, 8.0, ModRandom()),
                entityProvider,
                spawnPredicate,
                summonCircleBeforeSpawn
            ).tryPlacement(30)
        }
    }

    private fun buildMissileAction(canContinueAttack: () -> Boolean): ActionWithConstantCooldown {
        val throwMissilesAction: () -> Unit = {
            target?.let {
                val target = it.boundingBox.center
                for (offset in getMissileLaunchOffsets()) {
                    missileThrower(offset).throwProjectile(target.add(offset.planeProject(VecUtils.yAxis)))
                }
                playVolleyShootSound()
            }
        }

        val readyMissilesAction = {
            MaelstromMod.serverEventScheduler.addEvent(TimedEvent(throwMissilesAction,
                missileThrowDelay, shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonMissileStatus)
            playVolleyPrepareSound()
        }

        return ActionWithConstantCooldown(readyMissilesAction, missileThrowCooldown)
    }

    private fun buildCometAction(canContinueAttack: () -> Boolean): ActionWithConstantCooldown {
        val throwCometAction = {
            ThrowProjectileAction(this, cometThrower(getCometLaunchPosition())).perform()
            playCometLaunchSound()
        }

        val readyCometAction = {
            MaelstromMod.serverEventScheduler.addEvent(TimedEvent(throwCometAction,
                cometThrowDelay,
                shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonCometStatus)
            playCometPrepareSound()
        }

        return ActionWithConstantCooldown(readyCometAction, cometThrowCooldown)
    }

    private fun buildAttackMovement(): VelocityGoal {
        val tooCloseToTarget: (Vec3d) -> Boolean =
            getWithinDistancePredicate(tooCloseToTargetDistance) { this.target!!.pos }
        val tooFarFromTarget: (Vec3d) -> Boolean =
            { !getWithinDistancePredicate(tooFarFromTargetDistance) { this.target!!.pos }(it) }
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

    private fun buildWanderGoal(): VelocityGoal {
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
        positionalHistory.set(pos)
        if (isInvulnerable) {
            (world as ServerWorld).spawnParticles(Particles.SKELETON, pos.x, pos.y + 5, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun damage(source: DamageSource?, amount: Float): Boolean {
        damageHistory.set(age)

        if (target == null) {
            val attacker = source?.attacker
            if (attacker is LivingEntity) {
                buildTeleportAction({ isAlive }, { attacker }).perform()
            }
        }

        return super.damage(source, amount)
    }

    override fun handleStatus(status: Byte) {
        if (status == summonCometStatus) {
            doIdleAnimation = false
            doCometAttackAnimation.flag()
            MaelstromMod.clientEventScheduler.addEvent(
                TimedEvent({ summonCometParticleBuilder.build(eyePos().add(getCometLaunchPosition())) },
                    cometParticleSummonDelay,
                    cometThrowDelay - cometParticleSummonDelay,
                    ::shouldCancelAttackAnimation))
        }
        if (status == summonMissileStatus) {
            doIdleAnimation = false
            doMissileAttackAnimation.flag()
            MaelstromMod.clientEventScheduler.addEvent(
                TimedEvent({
                    for (offset in getMissileLaunchOffsets()) {
                        summonMissileParticleBuilder.build(eyePos().add(offset))
                    }
                },
                    missileParticleSummonDelay,
                    missileThrowDelay - missileParticleSummonDelay,
                    ::shouldCancelAttackAnimation))
        }
        if (status == summonMinionsStatus) {
            doIdleAnimation = false
            doMinionAttackAnimation.flag()
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                minionSummonParticleBuilder.build(eyePos()
                    .add(VecUtils.yAxis.multiply(1.0))
                    .add(RandomUtils.randVec()
                        .planeProject(VecUtils.yAxis)
                        .normalize()
                        .multiply(getRandom().nextGaussian())))
            },
                minionSummonParticleDelay,
                minionSummonDelay - minionSummonParticleDelay,
                ::shouldCancelAttackAnimation))
        }
        if (status == teleportStatus) {
            doIdleAnimation = false
            doTeleportAnimation.flag()
            MaelstromMod.clientEventScheduler.addEvent(
                TimedEvent(::spawnTeleportParticles,
                    beginTeleportParticleDelay,
                    teleportParticleDuration,
                    ::shouldCancelAttackAnimation))
        }
        if (status == successfulTeleportStatus) {
            doEndTeleportAnimation.flag()
            MaelstromMod.clientEventScheduler.addEvent(
                TimedEvent(::spawnTeleportParticles, 1, teleportParticleDuration, ::shouldCancelAttackAnimation))
        }
        if (status == fireballRageStatus) {
            doIdleAnimation = false
            doRageAnimation.flag()
            val numComets = getRageCometOffsets().size
            for (i in 0 until numComets) {
                MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                    val cometOffset = getRageCometOffsets()[i]
                    summonCometParticleBuilder.build(cometOffset.add(eyePos()))
                }, i * delayBetweenRageComets, initialRageCometDelay, ::shouldCancelAttackAnimation))
            }
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                MathUtils.circleCallback(3.0, 72, rotationVector) {
                    flameRingFactory.build(it.add(eyePos()))
                }
            }, 0, rageCometsMoveDuration, ::shouldCancelAttackAnimation))
        }
        if (status == missileRageStatus) {
            doIdleAnimation = false
            doRageAnimation.flag()
            val numVolleys = getRageMissileVolleys().size
            for (i in 0 until numVolleys) {
                MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
                    for (offset in getRageMissileVolleys()[i]) {
                        summonMissileParticleBuilder.build(eyePos().add(offset))
                    }
                },
                    ragedMissileParticleDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                    ragedMissileVolleyBetweenVolleyDelay,
                    ::shouldCancelAttackAnimation))
            }
        }
        if (status == stopAttackStatus) {
            doIdleAnimation = true
        }
        super.handleStatus(status)
    }

    override fun collides(): Boolean {
        return collides
    }

    private fun spawnTeleportParticles() {
        teleportParticleBuilder.build(eyePos()
            .add(RandomUtils.randVec()
                .multiply(3.0)))
    }

    private fun getCometLaunchPosition() = VecUtils.yAxis.multiply(2.0)
    private fun getMissileLaunchOffsets(): List<Vec3d> = listOf(
        MathUtils.axisOffset(rotationVector, VecUtils.yAxis.add(VecUtils.zAxis.multiply(2.0))),
        MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(1.5).add(VecUtils.zAxis)),
        MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(2.0)),
        MathUtils.axisOffset(rotationVector, VecUtils.yAxis.multiply(1.5).add(VecUtils.zAxis.negateServer())),
        MathUtils.axisOffset(rotationVector, VecUtils.yAxis.add(VecUtils.zAxis.negateServer().multiply(2.0)))
    )

    private fun getRageCometOffsets(): List<Vec3d> {
        val offsets = mutableListOf<Vec3d>()
        MathUtils.circleCallback(3.0, numCometsDuringRage, rotationVector) { offsets.add(it) }
        return offsets
    }

    private fun getRageMissileVolleys(): List<List<Vec3d>> {
        val xOffset = 3.0
        val zOffset = 4.0
        val numPoints = 9
        val lineStart = MathUtils.axisOffset(rotationVector, VecUtils.xAxis
            .multiply(xOffset)
            .add(VecUtils.zAxis.multiply(zOffset)))
        val lineEnd = MathUtils.axisOffset(rotationVector, VecUtils.xAxis
            .multiply(xOffset)
            .add(VecUtils.zAxis.multiply(-zOffset)))
        val lineAcross = mutableListOf<Vec3d>()
        MathUtils.lineCallback(lineStart, lineEnd, numPoints) { v, _ -> lineAcross.add(v) }
        val lineUpDown = lineAcross.map { it.rotateVector(rotationVector, 90.0) }
        val cross = lineAcross + lineUpDown
        val xVolley = cross.map { it.rotateVector(rotationVector, 45.0) }

        return listOf(lineAcross, lineUpDown, cross, xVolley)
    }

    private fun canContinueAttack() = isAlive && target != null
    private fun shouldCancelAttackAnimation() = !isAlive || doIdleAnimation

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