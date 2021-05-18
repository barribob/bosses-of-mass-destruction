package net.barribob.boss.cardinalComponents

import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk

class ChunkBlockCacheComponent(val chunk: Chunk) : IChunkBlockCacheComponent {
    private val map = hashMapOf<Block, MutableSet<BlockPos>>()

    override fun addToChunk(block: Block, pos: BlockPos) {
        val m = map[block] ?: mutableSetOf()
        m.add(pos)
        map[block] = m
    }

    override fun getBlocksFromChunk(block: Block): List<BlockPos> = map[block]?.toList() ?: listOf()

    override fun removeFromChunk(block: Block, pos: BlockPos) {
        map[block]?.remove(pos)
    }

    override fun readFromNbt(p0: CompoundTag) {
    }

    override fun writeToNbt(p0: CompoundTag) {
    }
}