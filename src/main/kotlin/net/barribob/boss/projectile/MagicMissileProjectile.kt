package net.barribob.boss.projectile

import net.barribob.boss.mob.Entities
import net.barribob.boss.projectile.util.ExemptEntities
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class MagicMissileProjectile : BaseThrownItemEntity {
    private var entityHit: ((LivingEntity) -> Unit)? = null

    constructor(
        d: Double,
        e: Double,
        f: Double,
        world: World,
    ) : super(Entities.MAGIC_MISSILE, d, e, f, world)

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world)

    constructor(livingEntity: LivingEntity, world: World, entityHit: (LivingEntity) -> Unit, exemptEntities: List<EntityType<*>>) : super(
        Entities.MAGIC_MISSILE,
        livingEntity,
        world,
        ExemptEntities(exemptEntities)
    ) {
        this.entityHit = entityHit
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        val entity = entityHitResult.entity
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            entity.damage(
                DamageSource.thrownProjectile(this, owner),
                owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            )
            if (entity is LivingEntity) {
                entityHit?.let { it(entity ) }
            }
        }
        remove()
    }

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        super.onBlockHit(blockHitResult)
        playSound(SoundEvents.BLOCK_BASALT_BREAK, 1.0f, 1.0f)
        remove()
    }
}