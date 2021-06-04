package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Tickable
import net.minecraft.util.math.ChunkPos

open class ChunkCacheBlockEntity(private val block: Block, type: BlockEntityType<*>?) : BlockEntity(type), Tickable {
    private var added = false

    override fun tick() {
        val world = world ?: return
        if (!added) {
            ModComponents.getChunkBlockCache(world).ifPresent {
                it.addToChunk(ChunkPos(pos), block, pos)
                added = true
            }
        }
    }

    override fun markRemoved() {
        val world = world
        if(world != null) {
            ModComponents.getChunkBlockCache(world).ifPresent {
                it.removeFromChunk(ChunkPos(pos), block, pos)
            }
            added = false
        }
        super.markRemoved()
    }
}