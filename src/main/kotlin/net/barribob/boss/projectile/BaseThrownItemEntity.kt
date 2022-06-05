package net.barribob.boss.projectile

import net.barribob.boss.Mod
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.network.Packet
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimationTickable
import java.util.function.Predicate

abstract class BaseThrownItemEntity : ThrownItemEntity, IAnimationTickable {
    protected val entityCollisionPredicate: Predicate<EntityHitResult>
    protected open val collisionPredicate: Predicate<HitResult> = Predicate<HitResult> { !world.isClient }

    constructor(
        entityType: EntityType<out ThrownItemEntity>, world: World?
    ) : super(entityType, world) {
        entityCollisionPredicate = Predicate { true }
    }

    constructor(
        entityType: EntityType<out ThrownItemEntity>,
        livingEntity: LivingEntity,
        world: World,
        collisionPredicate: Predicate<EntityHitResult> = Predicate { true }
    ) : super(
        entityType,
        livingEntity,
        world,
    ) {
        this.entityCollisionPredicate = collisionPredicate
    }

    final override fun tick() {
        super.tick()
        if (world.isClient) {
            clientTick()
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        return Mod.networkUtils.createClientEntityPacket(this)
    }

    override fun onCollision(hitResult: HitResult) {
        if(collisionPredicate.test(hitResult)) {
            super.onCollision(hitResult)
        }
    }

    final override fun onEntityHit(entityHitResult: EntityHitResult) {
        if(entityCollisionPredicate.test(entityHitResult)) {
            entityHit(entityHitResult)
        }
    }

    abstract fun entityHit(entityHitResult: EntityHitResult)

    open fun clientTick() {
    }

    override fun getDefaultItem(): Item = Items.SNOWBALL

    override fun tickTimer(): Int {
        return age
    }
}