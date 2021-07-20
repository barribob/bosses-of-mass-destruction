package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld

class LightBlockRemover(private val entity: LivingEntity) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        ++entity.deathTime
        if (entity.deathTime == 20 && !world.isClient()) {
            if (world.getBlockState(entity.blockPos).block == Blocks.LIGHT) {
                world.setBlockState(entity.blockPos, Blocks.AIR.defaultState)
            }
            world.sendEntityStatus(entity, 60.toByte())
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }
}