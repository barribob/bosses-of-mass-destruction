package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.ObsidilithConfig
import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.mob.utils.StatusImmunity
import net.barribob.boss.mob.utils.animation.AnimationPredicate
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class ObsidilithEntity(
    entityType: EntityType<out ObsidilithEntity>,
    world: World,
    private val mobConfig: ObsidilithConfig
) : BaseEntity(entityType, world) {
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PURPLE, BossBar.Style.NOTCHED_12)
    var currentAttack: Byte = 0
    private val statusRegistry = mapOf(
        Pair(ObsidilithUtils.burstAttackStatus, BurstAction(this)),
        Pair(ObsidilithUtils.waveAttackStatus, WaveAction(this)),
        Pair(ObsidilithUtils.spikeAttackStatus, SpikeAction(this)),
        Pair(ObsidilithUtils.anvilAttackStatus, AnvilAction(this, mobConfig.anvilAttackExplosionStrength)),
        Pair(ObsidilithUtils.pillarDefenseStatus, PillarAction(this))
    )
    private val moveLogic = ObsidilithMoveLogic(statusRegistry, this)
    private val effectHandler = ObsidilithEffectHandler(this, ModComponents.getWorldEventScheduler(world))
    override val damageHandler = CompositeDamageHandler(moveLogic, ShieldDamageHandler(::isShielded))
    private val activePillars = mutableSetOf<BlockPos>()
    private val iEntity = EntityAdapter(this)
    override val statusEffectHandler = StatusImmunity(StatusEffects.WITHER, StatusEffects.POISON)

    init {
        ignoreCameraFrustum = true

        if (!world.isClient && world is ServerWorld) {
            goalSelector.add(1, buildAttackGoal())

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))

            preTickEvents.addEvent(TimedEvent({
                world.playSound(pos, Mod.sounds.waveIndicator, SoundCategory.HOSTILE, 1.5f, 0.7f)
            }, 1))
        }
    }

    private fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(moveLogic, 80)
        return ActionGoal(
            ::canContinueAttack,
            tickAction = attackAction,
            endAction = attackAction
        )
    }

    override fun serverTick(serverWorld: ServerWorld) {
        super.serverTick(serverWorld)

        LichUtils.cappedHeal(
            iEntity,
            EntityStats(this),
            ObsidilithUtils.hpPillarShieldMilestones,
            mobConfig.idleHealingPerTick,
            ::heal
        )

        activePillars.removeIf {
            world.getBlockState(it).block != ModBlocks.obsidilithRune || !it.isWithinDistance(
                blockPos,
                64.0
            )
        }
        getDataTracker().set(ObsidilithUtils.isShielded, activePillars.any())

        if (this.age % 40 == 0) {
            activePillars.randomOrNull()?.let {
                MathUtils.lineCallback(it.asVec3d().add(0.5, 0.5, 0.5), eyePos(), 15) { vec3d, i ->
                    preTickEvents.addEvent(TimedEvent({
                        serverWorld.spawnParticle(Particles.PILLAR_RUNE, vec3d, Vec3d.ZERO)
                    }, i))
                }
            }
        }
    }

    override fun isCollidable(): Boolean = true

    override fun pushAwayFrom(entity: Entity) {
    }

    override fun onDeath(source: DamageSource?) {
        if (mobConfig.spawnPillarOnDeath) {
            ObsidilithUtils.onDeath(this, mobConfig.experienceDrop)
            if(world.isClient) effectHandler.handleStatus(ObsidilithUtils.deathStatus)
        }

        super.onDeath(source)
    }

    private fun canContinueAttack() = isAlive && target != null

    override fun handleStatus(status: Byte) {
        val attackStatus = statusRegistry[status]
        if (attackStatus != null) {
            effectHandler.handleStatus(status)
            currentAttack = status
            preTickEvents.addEvent(TimedEvent({ currentAttack = 0 }, 40))
        }
        super.handleStatus(status)
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(ObsidilithUtils.isShielded, false)
    }

    override fun getHurtSound(source: DamageSource?) = Mod.sounds.obsidilithHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.obsidilithDeath

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        data.addAnimationController(AnimationController(this, "summon", 0f, AnimationPredicate<ObsidilithEntity> {
                it.controller.setAnimation(
                    AnimationBuilder().addAnimation("summon", false)
                )
                PlayState.CONTINUE
        }))
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun getArmor(): Int = if (target != null) super.getArmor() else 24

    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean {
        return false
    }

    fun isShielded(): Boolean = getDataTracker().get(ObsidilithUtils.isShielded)

    fun addActivePillar(pos: BlockPos) {
        activePillars.add(pos)
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putIntArray(::activePillars.name, activePillars.flatMap { listOf(it.x, it.y, it.z) })
        return super.writeNbt(tag)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.contains(::activePillars.name)) {
            activePillars.addAll(
                nbt.getIntArray(::activePillars.name).asIterable().chunked(3).map { BlockPos(it[0], it[1], it[2]) })
        }
    }
}