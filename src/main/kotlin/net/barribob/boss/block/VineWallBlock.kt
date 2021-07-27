package net.barribob.boss.block

import net.minecraft.block.BlockState
import net.minecraft.block.PaneBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.*

class VineWallBlock(settings: Settings?) : PaneBlock(settings) {
    override fun scheduledTick(state: BlockState?, world: ServerWorld, pos: BlockPos, random: Random?) {
        world.breakBlock(pos, false)
    }
}