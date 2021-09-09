package net.barribob.boss.mob.mobs.void_blossom

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.Mod
import net.barribob.boss.config.VoidBlossomConfig
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.mobs.void_blossom.hitbox.NetworkedHitboxManager
import net.barribob.boss.mob.mobs.void_blossom.hitbox.VoidBlossomHitboxes
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.CompositeEntityTick
import net.barribob.boss.mob.utils.CompositeStatusHandler
import net.barribob.boss.utils.ModUtils
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class VoidBlossomEntity(entityType: EntityType<out PathAwareEntity>, world: World, config: VoidBlossomConfig) : BaseEntity(entityType, world),
    MultipartAwareEntity {
    private val animationHolder = AnimationHolder(
        this, mapOf(
            Pair(VoidBlossomAttacks.spikeAttack, AnimationHolder.Animation("spike", "idle")),
            Pair(VoidBlossomAttacks.spikeWaveAttack, AnimationHolder.Animation("spike_wave", "idle")),
            Pair(VoidBlossomAttacks.sporeAttack, AnimationHolder.Animation("spore", "idle")),
            Pair(VoidBlossomAttacks.bladeAttack, AnimationHolder.Animation("leaf_blade", "idle")),
            Pair(VoidBlossomAttacks.blossomAction, AnimationHolder.Animation("blossom", "idle")),
            Pair(3, AnimationHolder.Animation("death", "idle")),
        ),
        VoidBlossomAttacks.stopAttackAnimation,
        0f
    )
    private val hitboxes = VoidBlossomHitboxes(this)
    private val hitboxHelper = NetworkedHitboxManager(this, hitboxes.getMap())
    override val statusHandler = CompositeStatusHandler(
        animationHolder,
        ClientSporeEffectHandler(this, preTickEvents),
        ClientDeathEffectHandler(this, preTickEvents)
    )
    private var shouldSpawnBlossoms = BooleanFlag()
    private val hpDetector = StagedDamageHandler(hpMilestones) { shouldSpawnBlossoms.flag() }
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.GREEN, BossBar.Style.NOTCHED_12)
    val clientSpikeHandler = VoidBlossomClientSpikeHandler()
    override val clientTick = clientSpikeHandler
    override val serverTick = CompositeEntityTick(
        LightBlockPlacer(this),
        VoidBlossomSpikeTick(this),
        hitboxes.getTickers(),
        CappedHeal(this, hpMilestones, config.idleHealingPerTick))
    override val deathServerTick = CompositeEntityTick(
        LightBlockRemover(this),
        VoidBlossomDropExpDeathTick(this, preTickEvents, config.experienceDrop)
    )
    override val damageHandler = CompositeDamageHandler(hpDetector, hitboxes.getDamageHandlers())
    init {
        ignoreCameraFrustum = true

        if (!world.isClient && world is ServerWorld) {
            val attackHandler = VoidBlossomAttacks(this, preTickEvents) { shouldSpawnBlossoms.getAndReset() }
            goalSelector.add(2, CompositeGoal()) // Idle goal
            goalSelector.add(1, CompositeGoal(attackHandler.buildAttackGoal(), ActionGoal(::canContinueAttack, tickAction = ::lookAtTarget)))
            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))
        }
    }

    private fun lookAtTarget() {
        val target = target
        if (target != null) {
            lookControl.lookAt(target.eyePos())
            lookAtEntity(target, bodyYawSpeed.toFloat(), lookPitchSpeed.toFloat())
        }
    }

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        animationHolder.registerControllers(data)
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    private fun canContinueAttack() = isAlive && target != null
    override fun isOnFire() = false
    override fun getCompoundBoundingBox(bounds: Box?): CompoundOrientedBox = hitboxHelper.getBounds().getBox(bounds)
    override fun getBounds(): EntityBounds = hitboxHelper.getBounds()
    override fun isInsideWall(): Boolean = false
    override fun getHurtSound(source: DamageSource?): SoundEvent = Mod.sounds.voidBlossomHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.voidBlossomHurt
    override fun getSoundVolume(): Float = 1.5f
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)
    override fun getArmor(): Int = if (target != null) super.getArmor() else 20

    override fun onSetPos(x: Double, y: Double, z: Double) {
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }

    companion object {
        val hpMilestones = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    }
}