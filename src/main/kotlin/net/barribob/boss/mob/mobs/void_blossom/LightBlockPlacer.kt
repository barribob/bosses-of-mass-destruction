package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld

class LightBlockPlacer(private val entity: Entity) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        if (world.getBlockState(entity.blockPos) != Blocks.LIGHT) {
            world.setBlockState(entity.blockPos, Blocks.LIGHT.defaultState)
        }
    }
}