package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentV3
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos

interface IChunkBlockCacheComponent : ComponentV3 {
    fun addToChunk(block: Block, pos: BlockPos)
    fun getBlocksFromChunk(block: Block): List<BlockPos>
    fun removeFromChunk(block: Block, pos: BlockPos)
}