package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

open class ChunkCacheBlockEntity(
    private val block: Block, type: BlockEntityType<*>?,
    pos: BlockPos?, state: BlockState?
) : BlockEntity(type, pos, state) {
    private var added = false

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: ChunkCacheBlockEntity) {
            if (!entity.added) {
                ModComponents.getChunkBlockCache(world).ifPresent {
                    it.addToChunk(ChunkPos(pos), entity.block, pos)
                    entity.added = true
                }
            }
        }
    }

    override fun markRemoved() {
        val world = world
        if (world != null) {
            ModComponents.getChunkBlockCache(world).ifPresent {
                it.removeFromChunk(ChunkPos(pos), block, pos)
            }
            added = false
        }
        super.markRemoved()
    }
}