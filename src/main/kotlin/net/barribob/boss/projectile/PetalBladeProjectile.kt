package net.barribob.boss.projectile

import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity
import net.barribob.boss.projectile.util.ExemptEntities
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class PetalBladeProjectile : BaseThrownItemEntity {
    private var entityHit: ((LivingEntity) -> Unit)? = null

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world) {
        dataTracker.startTracking(renderRotation, 0f)
    }


    constructor(livingEntity: LivingEntity, world: World, entityHit: (LivingEntity) -> Unit, exemptEntities: List<EntityType<*>>, rotation: Float) : super(
        Entities.PETAL_BLADE,
        livingEntity,
        world,
        ExemptEntities(exemptEntities)
    ) {
        dataTracker.startTracking(renderRotation, rotation)
        this.entityHit = entityHit
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        val entity = entityHitResult.entity
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            entity.damage(
                entity.world.damageSources.thrown(this, owner),
                owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            )
            if (entity is LivingEntity) {
                entityHit?.let { it(entity ) }
            }
        }
        discard()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        super.onBlockHit(blockHitResult)
        discard()
    }

    companion object {
        val renderRotation: TrackedData<Float> = DataTracker.registerData(GauntletEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }
}