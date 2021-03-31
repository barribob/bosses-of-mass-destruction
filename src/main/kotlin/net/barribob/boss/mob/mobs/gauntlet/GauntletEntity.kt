package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.BossVisibilityCache
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
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.util.math.BlockPos
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
    private val animationHandler = GauntletAnimations(this)
    private val visibilityCache = BossVisibilityCache(this)
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

    override fun setPos(x: Double, y: Double, z: Double) {
        super.setPos(x, y, z)
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun isClimbing(): Boolean = false
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false
    override fun getLookPitchSpeed(): Int = 90
    override fun getBoundingBox(): CompoundOrientedBox = hitboxHelper.getHitbox().getBox(super.getBoundingBox())
    override fun getBounds(): EntityBounds = hitboxHelper.getHitbox()
    override fun getActiveEyeHeight(pose: EntityPose, dimensions: EntityDimensions) = dimensions.height * 0.4f
    override fun isInsideWall(): Boolean = false
    override fun getArmor(): Int = if (target != null) super.getArmor() else 24
    override fun getAmbientSound() = Mod.sounds.gauntletIdle
    override fun getHurtSound(source: DamageSource?) = Mod.sounds.gauntletHurt
    override fun getDeathSound() = Mod.sounds.gauntletDeath
    override fun getSoundVolume() = 2.0f
    override fun getVisibilityCache() = visibilityCache
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)
}
