package net.barribob.boss.block

import net.barribob.boss.mob.Entities
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class ObsidilithRuneBlock(settings: Settings) : Block(settings) {
    private fun linkToEntities(world: ServerWorld, pos: BlockPos) {
        world.getEntitiesByType(Entities.OBSIDILITH, Box(pos).expand(15.0, 40.0, 15.0)) { true }.forEach {
            it.addActivePillar(pos)
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        linkToEntities(world, pos)
    }

    override fun onBlockAdded(
        state: BlockState,
        world: World,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean
    ) {
        world.createAndScheduleBlockTick(pos, this, 10)
    }
}