package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.io.ILogger
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.World

class CompoundTagEntityProvider(private val tag: CompoundTag, val world: World, private val logger: ILogger): IEntityProvider {
    override fun getEntity(): Entity? {
        val entity = EntityType.loadEntityWithPassengers(tag, world) { it }

        if(entity == null) {
            logger.warn("Failed to create entity from tag: $tag")
            return null
        }

        return entity
    }

}