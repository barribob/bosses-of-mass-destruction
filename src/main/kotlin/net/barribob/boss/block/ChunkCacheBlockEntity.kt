package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Tickable
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory

class ChunkCacheBlockEntity(private val block: Block, type: BlockEntityType<*>?) : BlockEntity(type), Tickable, IAnimatable {
    private var added = false

    override fun tick() {
        if (!added) {
            world?.getChunk(pos)?.let { chunk ->
                ModComponents.getChunkBlockCache(chunk).ifPresent {
                    it.addToChunk(block, pos)
                    added = true
                }
            }
        }
    }

    override fun markRemoved() {
        world?.getChunk(pos)?.let { chunk ->
            ModComponents.getChunkBlockCache(chunk).ifPresent {
                it.removeFromChunk(block, pos)
                added = false
            }
        }
        super.markRemoved()
    }

    override fun registerControllers(data: AnimationData) {
    }

    private val animationFactory = AnimationFactory(this)
    override fun getFactory(): AnimationFactory = animationFactory
}