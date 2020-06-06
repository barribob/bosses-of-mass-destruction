package net.barribob.maelstrom.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.world.World

class MaelstromScoutEntity(entityType: EntityType<out MobEntity>, world: World) : MobEntity(entityType, world) {
    override fun getAttributes(): AttributeContainer {
        return AttributeContainer(HostileEntity.createHostileAttributes().build())
    }
}