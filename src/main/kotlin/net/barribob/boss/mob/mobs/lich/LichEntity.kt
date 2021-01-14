package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.BossVisibilityCache
import net.barribob.boss.mob.ai.ValidatedTargetSelector
import net.barribob.boss.mob.ai.VelocitySteering
import net.barribob.boss.mob.ai.action.*
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.ai.goals.VelocityGoal
import net.barribob.boss.mob.ai.valid_direction.CanMoveThrough
import net.barribob.boss.mob.ai.valid_direction.InDesiredRange
import net.barribob.boss.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamagedAttackerNotSeen
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.mobs.lich.LichUtils.hpPercentRageModes
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.mob.utils.*
import net.barribob.boss.mob.utils.animation.AnimationPredicate
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.utils.AnimationUtils
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.boss.utils.VanillaCopies
import net.barribob.boss.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.io.config.IConfig
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityGroup
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.Difficulty
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World, mobConfig: IConfig) : BaseEntity(
    entityType,
    world,
    mobConfig
) {
    private val cometExplosionStrength = mobConfig.getFloat("comet_explosion_strength")
    private val missileStatusEffect =
        Registry.STATUS_EFFECT.getOrEmpty(Identifier(mobConfig.getString("missile.status_effect_id")))
    private val missileStatusDuration = mobConfig.getInt("missile.status_effect_duration")
    private val missileStatusPotency = mobConfig.getInt("missile.status_effect_potency")
    private val healingStrength = mobConfig.getFloat("idle_healing_per_tick")
    private val summonId = mobConfig.getString("summon.mob_id")
    private val summonNbt = NbtUtils.readDefaultNbt(Mod.LOGGER, mobConfig.getConfig("summon.summon_nbt"))
    private val summonEntityType = Registry.ENTITY_TYPE[Identifier(summonId)]
    val shouldSetToNighttime = mobConfig.getBoolean("eternal_nighttime")
    private val experienceDrop = mobConfig.getInt("experience_drop")

    val velocityHistory = HistoricalData(Vec3d.ZERO)
    private val positionalHistory = HistoricalData(Vec3d.ZERO, 10)
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
    private val hpBelowThresholdStatus: Byte = 12
    private val minionRageStatus: Byte = 13
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
    private val delayTimes = (0 until numMobs)
        .map { MathUtils.consecutiveSum(0, it) }
        .mapIndexed { index, i -> initialSpawnTimeCooldown + (index * initialBetweenSpawnDelay) - (i * spawnDelayDecrease) }
    private val totalMoveTime = delayTimes.last() + minionRuneToMinionSpawnDelay

    private val teleportCooldown = 80
    private val teleportStartSoundDelay = 10
    private val teleportDelay = 40
    private val beginTeleportParticleDelay = 15
    private val teleportParticleDuration = 10

    private val numCometsDuringRage = 6
    private val initialRageCometDelay = 60
    private val delayBetweenRageComets = 30
    private val rageCometsMoveDuration = initialRageCometDelay + (numCometsDuringRage * delayBetweenRageComets)

    private val ragedMissileVolleyInitialDelay = 60
    private val ragedMissileVolleyBetweenVolleyDelay = 30
    private val ragedMissileParticleDelay = 30

    private val tooFarFromTargetDistance = 35.0
    private val tooCloseToTargetDistance = 20.0
    private val idleWanderDistance = 50.0
    private val iEntity = EntityAdapter(this)

    private val visibilityCache = BossVisibilityCache(this)
    override val damageHandler = CompositeDamageHandler(listOf(
        StagedDamageHandler(hpPercentRageModes) {
            priorityMoves.addAll(listOf(
                cometRageAction, volleyRageAction, minionRageAction
            ))
            world.sendEntityStatus(this, hpBelowThresholdStatus)
        },
        DamagedAttackerNotSeen(iEntity) { buildTeleportAction({ isAlive }, { it }) }))
    private val priorityMoves = mutableListOf<IActionWithCooldown>()
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.BLUE, BossBar.Style.PROGRESS)

    private val summonMissileParticleBuilder = ParticleFactories.soulFlame().age { 2 }.colorVariation(0.5)
    private val teleportParticleBuilder = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color { ModColors.TELEPORT_PURPLE }
        .age { RandomUtils.range(10, 15) }
        .brightness { Particles.FULL_BRIGHT }
    private val summonCometParticleBuilder = ParticleFactories.cometTrail().colorVariation(0.5)
    private val flameRingFactory = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.WHITE.multiply(0.5)) }
        .brightness { Particles.FULL_BRIGHT }
        .age { RandomUtils.range(0, 7) }
    private val minionSummonParticleBuilder = ParticleFactories.soulFlame()
        .color { ModColors.WHITE }
        .velocity{ VecUtils.yAxis.multiply(RandomUtils.double(0.2) + 0.2) }
    private val thresholdParticleBuilder = ParticleFactories.soulFlame()
        .age { 20 }
        .scale { 0.5f }
        .velocity{ RandomUtils.randVec() }
    private val summonRingFactory = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
        .brightness { Particles.FULL_BRIGHT }
        .colorVariation(0.5)
        .age { 10 }
    private val summonRingCompleteFactory = ParticleFactories.soulFlame()
        .color { ModColors.WHITE }
        .age { RandomUtils.range(20, 30) }
    private val deathParticleFactory = ParticleFactories.soulFlame()
        .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
        .age { RandomUtils.range(40, 80) }
        .velocity { RandomUtils.randVec() }
        .colorVariation(0.5)
        .scale { 0.5f - (it * 0.3f) }

    private val missileThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = MagicMissileProjectile(this, world, {
                missileStatusEffect.ifPresent { effect ->
                    it.addStatusEffect(StatusEffectInstance(effect,
                        missileStatusDuration,
                        missileStatusPotency))
                }
            }, listOf(summonEntityType))
            projectile.setPos(eyePos().add(offset))
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    private val cometThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = CometProjectile(this, world, {
                world.createExplosion(this,
                    it.x,
                    it.y,
                    it.z,
                    cometExplosionStrength,
                    VanillaCopies.getEntityDestructionType(world))
            }, listOf(summonEntityType))
            projectile.setPos(eyePos().add(offset))
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

    private fun playCometLaunchSound() = playSound(Mod.sounds.cometShoot, 0.4f)
    private fun playCometPrepareSound() = playSound(Mod.sounds.cometPrepare, 0.4f)
    private fun playVolleyShootSound() = playSound(Mod.sounds.missileShoot, 1.0f)
    private fun playVolleyPrepareSound() = playSound(Mod.sounds.missilePrepare, 4.0f)
    private fun playBeginTeleportSound() = playSound(Mod.sounds.teleportPrepare, 3.0f)
    private fun playTeleportSound() = playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 2.0f)
    private fun playRageBeginSound() = playSound(Mod.sounds.ragePrepare, 1.0f)
    private fun playMinionRuneSound(pos: Vec3d) =
        world.playSound(pos, Mod.sounds.minionRune, SoundCategory.HOSTILE, 1.0f, range = 64.0)

    private fun playMinionSummonSound(entity: Entity) = entity.playSound(Mod.sounds.minionSummon, 0.7f, 1.0f)
    private fun playSound(soundEvent: SoundEvent, volume: Float) =
        world.playSound(pos, soundEvent, SoundCategory.HOSTILE, volume, range = 64.0)

    init {
        ignoreCameraFrustum = true

        if (!world.isClient) {
            goalSelector.add(1, SwimGoal(this))
            goalSelector.add(3, CompositeGoal(listOf(buildAttackGoal(), buildAttackMovement())))
            goalSelector.add(4, buildWanderGoal())

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) })
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
        val cooldownAction = {
            target?.let {
                val target = EntityAdapter(it)
                val regularMoveLogic = LichMoveLogic(cometThrowAction, missileAction, minionAction, teleportAction,
                    { positionalHistory }, { moveHistory }, ::inLineOfSight, iEntity, target)
                PrioritizedAttackAction({ priorityMoves },
                    regularMoveLogic::chooseRegularMove,
                    moveHistory).perform()
            } ?: 80
        }
        val attackAction = CooldownAction(cooldownAction, 80)
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
                    eventScheduler.addEvent(TimedEvent({
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
                    eventScheduler.addEvent(TimedEvent({
                        val target = it.boundingBox.center
                        cometThrower(offset).throwProjectile(target)
                        playCometLaunchSound()
                    }, initialRageCometDelay + (i * delayBetweenRageComets), shouldCancel = { !canContinueAttack() }))
                }
                world.sendEntityStatus(this, fireballRageStatus)
                playRageBeginSound()
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
                    { pos, entity -> spawnPredicate.canSpawn(pos, entity) && inLineOfSight(pos, EntityAdapter(it)) },
                    { pos, entity ->
                        world.sendEntityStatus(this, teleportStatus)
                        eventScheduler.addEvent(TimedEvent({
                            playBeginTeleportSound()
                            collides = false
                            eventScheduler.addEvent(TimedEvent({
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

    private fun inLineOfSight(pos: Vec3d, target: IEntity) = VanillaCopies.hasDirectLineOfSight(
        pos.add(newVec3d(y = standingEyeHeight.toDouble())),
        target.getPos(),
        world,
        this) &&
            MathUtils.facingSameDirection(target.getRotationVector(), MathUtils.unNormedDirection(target.getPos(), pos))

    private fun buildMinionRageAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val summonMobsAction = IAction {
            world.sendEntityStatus(this, minionRageStatus)
            for (delayTime in delayTimes) {
                eventScheduler.addEvent(TimedEvent({ beginSummonSingleMob(canContinueAttack) },
                    delayTime, shouldCancel = { !canContinueAttack() }))
            }
        }

        return ActionWithConstantCooldown(summonMobsAction, totalMoveTime)
    }

    private fun buildMinionAction(canContinueAttack: () -> Boolean): IActionWithCooldown {
        val summonMobsAction = IAction {
            world.sendEntityStatus(this, summonMinionsStatus)
            eventScheduler.addEvent(
                TimedEvent({ beginSummonSingleMob(canContinueAttack) },
                    minionSummonDelay,
                    shouldCancel = { !canContinueAttack() }))
        }

        return ActionWithConstantCooldown(summonMobsAction, minionSummonCooldown)
    }

    private fun beginSummonSingleMob(canContinueAttack: () -> Boolean) {
        val compoundTag = summonNbt.copy()
        compoundTag.putString("id", summonId)
        val serverWorld = world as ServerWorld
        val mobSpawner = SimpleMobSpawner(serverWorld)
        val entityProvider = CompoundTagEntityProvider(compoundTag, serverWorld, Mod.LOGGER)
        val spawnPredicate = MobEntitySpawnPredicate(world)
        val summonCircleBeforeSpawn: (pos: Vec3d, entity: Entity) -> Unit = { pos, entity ->
            serverWorld.spawnParticle(Particles.LICH_MAGIC_CIRCLE, pos, Vec3d.ZERO)
            playMinionRuneSound(pos)
            val spawnMobWithEffect = {
                mobSpawner.spawn(pos, entity)
                playMinionSummonSound(entity)
            }
            eventScheduler.addEvent(
                TimedEvent(spawnMobWithEffect, minionRuneToMinionSpawnDelay, shouldCancel = { !canContinueAttack() }))
        }

        target?.let {
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
            eventScheduler.addEvent(TimedEvent(throwMissilesAction,
                missileThrowDelay, shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonMissileStatus)
            eventScheduler.addEvent(
                TimedEvent(::playVolleyPrepareSound, 10, shouldCancel = { !canContinueAttack() }))
        }

        return ActionWithConstantCooldown(readyMissilesAction, missileThrowCooldown)
    }

    private fun buildCometAction(canContinueAttack: () -> Boolean): ActionWithConstantCooldown {
        val throwCometAction = {
            ThrowProjectileAction(this, cometThrower(getCometLaunchPosition())).perform()
            playCometLaunchSound()
        }

        val readyCometAction = {
            eventScheduler.addEvent(TimedEvent(throwCometAction,
                cometThrowDelay,
                shouldCancel = { !canContinueAttack() }))
            world.sendEntityStatus(this, summonCometStatus)
            eventScheduler.addEvent(
                TimedEvent(::playCometPrepareSound, 10, shouldCancel = { !canContinueAttack() }))
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
            iEntity,
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
            iEntity,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveTowards,
            createSteering(),
            targetSelector
        )
    }

    private fun createSteering() =
        VelocitySteering(iEntity, getAttributeValue(EntityAttributes.GENERIC_FLYING_SPEED), 120.0)

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

    override fun serverTick(serverWorld: ServerWorld) {
        positionalHistory.set(pos)

        LichUtils.cappedHeal(iEntity, this, hpPercentRageModes, healingStrength, ::heal)

        if (shouldSetToNighttime) {
            serverWorld.timeOfDay = LichUtils.timeToNighttime(serverWorld.timeOfDay)
        }
    }

    override fun handleStatus(status: Byte) {
        if (status == summonCometStatus) {
            doIdleAnimation = false
            doCometAttackAnimation.flag()
            eventScheduler.addEvent(
                TimedEvent({ summonCometParticleBuilder.build(eyePos().add(getCometLaunchPosition())) },
                    cometParticleSummonDelay,
                    cometThrowDelay - cometParticleSummonDelay,
                    ::shouldCancelAttackAnimation))
        }
        if (status == summonMissileStatus) {
            doIdleAnimation = false
            doMissileAttackAnimation.flag()
            eventScheduler.addEvent(
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
            eventScheduler.addEvent(TimedEvent({
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
        if (status == minionRageStatus) {
            doIdleAnimation = false
            doRageAnimation.flag()
            eventScheduler.addEvent(TimedEvent({
                animatedParticleMagicCircle(3.0, 30, 12, 0f)
                animatedParticleMagicCircle(6.0, 60, 24, 120f)
                animatedParticleMagicCircle(9.0, 90, 36, 240f)
            }, 10, shouldCancel = ::shouldCancelAttackAnimation))
        }
        if (status == teleportStatus) {
            doIdleAnimation = false
            doTeleportAnimation.flag()
            eventScheduler.addEvent(
                TimedEvent(::spawnTeleportParticles,
                    beginTeleportParticleDelay,
                    teleportParticleDuration,
                    ::shouldCancelAttackAnimation))
        }
        if (status == successfulTeleportStatus) {
            doEndTeleportAnimation.flag()
            eventScheduler.addEvent(
                TimedEvent(::spawnTeleportParticles, 1, teleportParticleDuration, ::shouldCancelAttackAnimation))
        }
        if (status == fireballRageStatus) {
            doIdleAnimation = false
            doRageAnimation.flag()
            val numComets = getRageCometOffsets().size
            for (i in 0 until numComets) {
                eventScheduler.addEvent(TimedEvent({
                    val cometOffset = getRageCometOffsets()[i]
                    summonCometParticleBuilder.build(cometOffset.add(eyePos()))
                }, i * delayBetweenRageComets, initialRageCometDelay, ::shouldCancelAttackAnimation))
            }
            eventScheduler.addEvent(TimedEvent({
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
                eventScheduler.addEvent(TimedEvent({
                    for (offset in getRageMissileVolleys()[i]) {
                        summonMissileParticleBuilder.build(eyePos().add(offset))
                    }
                },
                    ragedMissileParticleDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                    ragedMissileVolleyBetweenVolleyDelay,
                    ::shouldCancelAttackAnimation))
            }
        }
        if (status == hpBelowThresholdStatus) {
            for (i in 0 until 20) {
                thresholdParticleBuilder
                    .build(eyePos())
            }
        }
        if (status == stopAttackStatus) {
            doIdleAnimation = true
        }
        if (status.toInt() == 3) { // Death status
            eventScheduler.addEvent(TimedEvent({
                for(i in 0..4) {
                    deathParticleFactory.build(eyePos())
                }
            }, 0, 10))
        }
        super.handleStatus(status)
    }

    private fun animatedParticleMagicCircle(radius: Double, points: Int, time: Int, rotationDegrees: Float): Vec3d? {
        val spellPos = pos
        val circlePoints = MathUtils.circlePoints(radius, points, rotationVector)
        val timeScale = time / points.toFloat()
        circlePoints.mapIndexed { index, off ->
            eventScheduler.addEvent(TimedEvent({
                off.rotateY(rotationDegrees)
                summonRingFactory.build(off.add(spellPos))
            }, (index * timeScale).toInt()))
        }
        eventScheduler.addEvent(TimedEvent({
            circlePoints.map { summonRingCompleteFactory.build(it.add(spellPos)) }
        }, (points * timeScale).toInt()))
        return spellPos
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
    override fun getGroup(): EntityGroup = EntityGroup.UNDEAD

    override fun checkDespawn() = preventDespawnExceptPeaceful()
    override fun isDisallowedInPeaceful() = true

    override fun onDeath(source: DamageSource?) {
        val expTicks = 18
        val expPerTick = (experienceDrop / expTicks.toFloat()).toInt()
        eventScheduler.addEvent(TimedEvent({
            VanillaCopies.awardExperience(expPerTick, eyePos(), world)
        }, 0, expTicks))
        super.onDeath(source)
    }

    private fun preventDespawnExceptPeaceful() {
        if (world.difficulty == Difficulty.PEACEFUL && this.isDisallowedInPeaceful) {
            this.remove()
        } else {
            this.despawnCounter = 0
        }
    }

    override fun getHurtSound(source: DamageSource): SoundEvent = SoundEvents.ENTITY_WITHER_SKELETON_HURT
    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_WITHER_SKELETON_DEATH
    override fun getSoundVolume(): Float = 5.0f

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