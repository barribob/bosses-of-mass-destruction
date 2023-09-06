package net.barribob.boss.projectile.comet

import net.barribob.boss.mob.Entities
import net.barribob.boss.projectile.BaseThrownItemEntity
import net.barribob.boss.projectile.util.ExemptEntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoEntity
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.util.GeckoLibUtil

class CometProjectile : BaseThrownItemEntity, GeoEntity {
    private var impacted: Boolean = false
    private var impactAction: ((Vec3d) -> Unit)? = null

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world)

    constructor(livingEntity: LivingEntity, world: World, impactAction: (Vec3d) -> Unit, exemptEntities: List<EntityType<*>>) : super(
        Entities.COMET,
        livingEntity,
        world,
        ExemptEntities(exemptEntities)
    ) {
        this.impactAction = impactAction
    }

    private fun onImpact() {
        if (impacted) return

        impacted = true
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            impactAction?.let { it(pos) }
            discard()
        }
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        onImpact()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        onImpact()
        super.onBlockHit(blockHitResult)
    }

    override fun damage(source: DamageSource?, amount: Float): Boolean {
        onImpact()
        return super.damage(source, amount)
    }

    override fun collidesWith(other: Entity?): Boolean {
        return true
    }

    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
    }

    private val animationFactory = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory

    override fun getPitch() = age * 5f
}