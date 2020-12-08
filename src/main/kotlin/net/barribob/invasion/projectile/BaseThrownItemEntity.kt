package net.barribob.invasion.projectile

import net.barribob.invasion.utils.ModUtils
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.network.Packet
import net.minecraft.world.World

abstract class BaseThrownItemEntity : ThrownItemEntity {
    constructor(
        entityType: EntityType<out ThrownItemEntity>,
        d: Double,
        e: Double,
        f: Double,
        world: World
    ) : super(entityType, d, e, f, world)

    constructor(
        entityType: EntityType<out ThrownItemEntity>, world: World?
    ) : super(entityType, world)

    constructor(
        entityType: EntityType<out ThrownItemEntity>,
        livingEntity: LivingEntity,
        world: World
    ) : super(
        entityType,
        livingEntity,
        world,
    )

    final override fun tick() {
        super.tick()
        if (world.isClient) {
            clientTick()
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        return ModUtils.createClientEntityPacket(this)
    }

    open fun clientTick() {
    }

    override fun getDefaultItem(): Item = Items.SNOWBALL
}