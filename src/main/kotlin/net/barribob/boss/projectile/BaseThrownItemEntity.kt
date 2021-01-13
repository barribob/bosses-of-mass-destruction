package net.barribob.boss.projectile

import net.barribob.boss.utils.NetworkUtils
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.network.Packet
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import java.util.function.Predicate

abstract class BaseThrownItemEntity : ThrownItemEntity {
    private val collisionPredicate: Predicate<EntityHitResult>

    constructor(
        entityType: EntityType<out ThrownItemEntity>,
        d: Double,
        e: Double,
        f: Double,
        world: World
    ) : super(entityType, d, e, f, world) {
        collisionPredicate = Predicate { true }
    }

    constructor(
        entityType: EntityType<out ThrownItemEntity>, world: World?
    ) : super(entityType, world) {
        collisionPredicate = Predicate { true }
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
        this.collisionPredicate = collisionPredicate
    }

    final override fun tick() {
        super.tick()
        if (world.isClient) {
            clientTick()
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        return NetworkUtils.createClientEntityPacket(this)
    }

    override fun onCollision(hitResult: HitResult) {
        if(!world.isClient) {
            super.onCollision(hitResult)
        }
    }

    final override fun onEntityHit(entityHitResult: EntityHitResult) {
        if(collisionPredicate.test(entityHitResult)) {
            entityHit(entityHitResult)
        }
    }

    abstract fun entityHit(entityHitResult: EntityHitResult)

    open fun clientTick() {
    }

    override fun getDefaultItem(): Item = Items.SNOWBALL
}