package net.barribob.boss.block

import net.minecraft.block.BlockState
import net.minecraft.block.PaneBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class VineWallBlock(settings: Settings?) : PaneBlock(settings) {
    override fun scheduledTick(state: BlockState?, world: ServerWorld, pos: BlockPos, random: Random?) {
        world.breakBlock(pos, false)
    }
}