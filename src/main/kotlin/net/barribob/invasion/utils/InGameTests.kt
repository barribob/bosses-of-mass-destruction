package net.barribob.invasion.utils

import net.barribob.invasion.projectile.comet.CometProjectile
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.command.ServerCommandSource

object InGameTests {
    fun throwProjectile(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val projectile = CometProjectile(entity, entity.world)
            projectile.setItem(ItemStack(Items.SNOWBALL))
            projectile.setProperties(entity, entity.pitch, entity.yaw, 0f, 1.5f, 1.0f)
            entity.world.spawnEntity(projectile)
        }
    }
}