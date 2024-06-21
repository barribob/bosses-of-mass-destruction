package net.barribob.boss.cardinalComponents

import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.ladysnake.cca.api.v3.component.ComponentV3

interface IChunkBlockCacheComponent : ComponentV3 {
    fun addToChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos)
    fun getBlocksFromChunk(chunkPos: ChunkPos, block: Block): List<BlockPos>
    fun removeFromChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos)
}