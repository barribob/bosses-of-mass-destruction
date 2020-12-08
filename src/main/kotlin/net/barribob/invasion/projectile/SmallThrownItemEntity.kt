package net.barribob.invasion.projectile

import net.barribob.invasion.mob.Entities
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class SmallThrownItemEntity : BaseThrownItemEntity {
    constructor(
        d: Double,
        e: Double,
        f: Double,
        world: World,
    ) : super(Entities.TEST_PROJECTILE, d, e, f, world)

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world)

    constructor(livingEntity: LivingEntity, world: World) : super(
        Entities.TEST_PROJECTILE,
        livingEntity,
        world
    )

    override fun shouldRender(distance: Double): Boolean = false

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        val entity = entityHitResult.entity
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            entity.damage(
                DamageSource.thrownProjectile(this, owner),
                owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            )
        }
    }
}