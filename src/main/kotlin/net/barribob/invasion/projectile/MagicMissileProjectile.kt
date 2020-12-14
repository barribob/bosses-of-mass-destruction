package net.barribob.invasion.projectile

import net.barribob.invasion.mob.Entities
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class MagicMissileProjectile : BaseThrownItemEntity {
    constructor(
        d: Double,
        e: Double,
        f: Double,
        world: World,
    ) : super(Entities.MAGIC_MISSILE, d, e, f, world)

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world)

    constructor(livingEntity: LivingEntity, world: World) : super(
        Entities.MAGIC_MISSILE,
        livingEntity,
        world
    )

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        val entity = entityHitResult.entity
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            entity.damage(
                DamageSource.thrownProjectile(this, owner),
                owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            )
        }
        remove()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        super.onBlockHit(blockHitResult)
        remove()
    }
}