package net.barribob.boss.cardinalComponents

import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

class ChunkBlockCacheComponent(val world: World) : IChunkBlockCacheComponent {
    private val map = hashMapOf<ChunkPos, HashMap<Block, MutableSet<BlockPos>>>()

    override fun addToChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos) {
        val chunk = map[chunkPos] ?: hashMapOf()
        val blocks = chunk[block] ?: mutableSetOf()
        blocks.add(pos)
        chunk[block] = blocks
        map[chunkPos] = chunk
    }

    override fun getBlocksFromChunk(chunkPos: ChunkPos, block: Block): List<BlockPos> = map[chunkPos]?.get(block)?.toList() ?: listOf()

    override fun removeFromChunk(chunkPos: ChunkPos, block: Block, pos: BlockPos) {
        map[chunkPos]?.get(block)?.remove(pos)
    }

    override fun readFromNbt(p0: CompoundTag) {
    }

    override fun writeToNbt(p0: CompoundTag) {
    }
}