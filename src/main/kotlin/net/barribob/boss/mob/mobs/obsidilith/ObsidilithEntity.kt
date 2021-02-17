package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.ObsidilithConfig
import net.barribob.boss.mob.ai.BossVisibilityCache
import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class ObsidilithEntity(entityType: EntityType<out ObsidilithEntity>, world: World, val mobConfig: ObsidilithConfig) : BaseEntity(entityType, world) {
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PINK, BossBar.Style.NOTCHED_12)
    var currentAttack: Byte = 0
    private val statusRegistry = mapOf(
        Pair(ObsidilithUtils.burstAttackStatus, BurstAction(this)),
        Pair(ObsidilithUtils.waveAttackStatus, WaveAction(this)),
        Pair(ObsidilithUtils.spikeAttackStatus, SpikeAction(this)),
        Pair(ObsidilithUtils.anvilAttackStatus, AnvilAction(this)),
        Pair(ObsidilithUtils.pillarDefenseStatus, PillarAction(this))
    )
    private val moveLogic = ObsidilithMoveLogic(statusRegistry, this)
    private val effectHandler = ObsidilithEffectHandler(this, ModComponents.getWorldEventScheduler(world))
    override val damageHandler = CompositeDamageHandler(listOf(moveLogic, ShieldDamageHandler(::isShielded)))
    private val activePillars = mutableSetOf<BlockPos>()
    private val visibilityCache = BossVisibilityCache(this)
    private val iEntity = EntityAdapter(this)

    init {
        ignoreCameraFrustum = true

        if (!world.isClient) {
            goalSelector.add(1, buildAttackGoal())

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))
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

        if(this.age % 40 == 0) {
            activePillars.randomOrNull()?.let {
                MathUtils.lineCallback(it.asVec3d().add(0.5, 0.5, 0.5), eyePos(), 15) { vec3d, i ->
                    eventScheduler.addEvent(TimedEvent({
                        serverWorld.spawnParticle(Particles.PILLAR_RUNE, vec3d, Vec3d.ZERO)
                    }, i))
                }
            }
        }

        ObsidilithUtils.placeObsidianBelow(this)
    }

    override fun onDeath(source: DamageSource?) {
        if(mobConfig.spawnPillarOnDeath) {
            ObsidilithUtils.onDeath(this, mobConfig.experienceDrop)
            effectHandler.handleStatus(ObsidilithUtils.deathStatus)
        }

        super.onDeath(source)
    }

    private fun canContinueAttack() = isAlive && target != null

    override fun handleStatus(status: Byte) {
        val attackStatus = statusRegistry[status]
        if (attackStatus != null) {
            effectHandler.handleStatus(status)
            currentAttack = status
            eventScheduler.addEvent(TimedEvent({ currentAttack = 0 }, 40))
        }
        super.handleStatus(status)
    }

    override fun getVisibilityCache() = visibilityCache

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(ObsidilithUtils.isShielded, false)
    }

    override fun getHurtSound(source: DamageSource?): SoundEvent = SoundEvents.BLOCK_BASALT_HIT
    override fun getDeathSound(): SoundEvent = SoundEvents.BLOCK_BASALT_HIT

    override fun registerControllers(p0: AnimationData?) {
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun canHaveStatusEffect(effect: StatusEffectInstance): Boolean {
        return if (effect.effectType === StatusEffects.WITHER || effect.effectType === StatusEffects.POISON) {
            false
        } else {
            super.canHaveStatusEffect(effect)
        }
    }

    override fun getArmor(): Int = if(target != null) super.getArmor() else 24

    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean {
        return false
    }

    fun isShielded(): Boolean = getDataTracker().get(ObsidilithUtils.isShielded)

    fun addActivePillar(pos: BlockPos) {
        activePillars.add(pos)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putIntArray(::activePillars.name, activePillars.flatMap { listOf(it.x, it.y, it.z) })
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        if (tag.contains(::activePillars.name)) {
            activePillars.addAll(
                tag.getIntArray(::activePillars.name).asIterable().chunked(3).map { BlockPos(it[0], it[1], it[2]) })
        }
    }
}