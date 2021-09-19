package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.utils.*
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class GauntletEntity(entityType: EntityType<out PathAwareEntity>, world: World, mobConfig: GauntletConfig) :
    BaseEntity(entityType, world),
    MultipartAwareEntity {
    val hitboxHelper = GauntletHitboxes(this)
    val laserHandler = GauntletClientLaserHandler(this, postTickEvents)
    val energyShieldHandler = GauntletClientEnergyShieldHandler(this, postTickEvents)
    val clientBlindnessHandler = GauntletBlindnessIndicatorParticles(this, preTickEvents)
    private val gauntletGoalHandler = GauntletGoalHandler(this, goalSelector, targetSelector, postTickEvents, mobConfig)
    private val animationHandler = AnimationHolder(
        this, mapOf(
            Pair(GauntletAttacks.punchAttack, AnimationHolder.Animation("punch_start", "punch_loop")),
            Pair(GauntletAttacks.stopPunchAnimation, AnimationHolder.Animation("punch_stop", "idle")),
            Pair(GauntletAttacks.stopPoundAnimation, AnimationHolder.Animation("pound_stop", "idle")),
            Pair(GauntletAttacks.laserAttack, AnimationHolder.Animation("laser_eye_start", "laser_eye_loop")),
            Pair(GauntletAttacks.laserAttackStop, AnimationHolder.Animation("laser_eye_stop", "idle")),
            Pair(GauntletAttacks.swirlPunchAttack, AnimationHolder.Animation("swirl_punch", "idle")),
            Pair(GauntletAttacks.blindnessAttack, AnimationHolder.Animation("cast", "idle")),
            Pair(3, AnimationHolder.Animation("death", "idle"))
        ),
        GauntletAttacks.stopAttackAnimation
    )
    override val damageHandler = CompositeDamageHandler(hitboxHelper, gauntletGoalHandler)
    override val statusHandler = CompositeStatusHandler(animationHandler, laserHandler, clientBlindnessHandler)
    override val trackedDataHandler = CompositeTrackedDataHandler(laserHandler, energyShieldHandler)
    override val clientTick = laserHandler
    override val serverTick = IEntityTick<ServerWorld> { if (target == null) heal(mobConfig.idleHealingPerTick) }
    override val bossBar: ServerBossBar = ServerBossBar(displayName, BossBar.Color.RED, BossBar.Style.NOTCHED_6)
    override val statusEffectHandler = StatusImmunity(StatusEffects.WITHER, StatusEffects.POISON)
    override val moveHandler = gauntletGoalHandler
    override val nbtHandler = gauntletGoalHandler
    override val deathClientTick = ClientGauntletDeathHandler(this)
    override val deathServerTick = ServerGauntletDeathHandler(this, ModComponents.getWorldEventScheduler(world), mobConfig)

    init {
        ignoreCameraFrustum = true
        laserHandler.initDataTracker()
        energyShieldHandler.initDataTracker()
    }

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        animationHandler.registerControllers(data)
    }

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?,
    ) {
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this, 0.85f)
    }

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }

    override fun onSetPos(x: Double, y: Double, z: Double) {
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun isClimbing(): Boolean = false
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float, damageSource: DamageSource?) = false
    override fun getLookPitchSpeed(): Int = 90
    override fun getCompoundBoundingBox(box: Box): CompoundOrientedBox = hitboxHelper.getHitbox().getBox(box)
    override fun getBounds(): EntityBounds = hitboxHelper.getHitbox()
    override fun getActiveEyeHeight(pose: EntityPose, dimensions: EntityDimensions) = dimensions.height * 0.4f
    override fun isInsideWall(): Boolean = false
    override fun getArmor(): Int = if (target != null) super.getArmor() else 24
    override fun getAmbientSound() = Mod.sounds.gauntletIdle
    override fun getHurtSound(source: DamageSource?) = Mod.sounds.gauntletHurt
    override fun getDeathSound() = Mod.sounds.gauntletDeath
    override fun getSoundVolume() = 2.0f
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)

    companion object {
        val laserTarget: TrackedData<Int> =
            DataTracker.registerData(GauntletEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
        val isEnergized: TrackedData<Boolean> =
            DataTracker.registerData(GauntletEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
    }
}
