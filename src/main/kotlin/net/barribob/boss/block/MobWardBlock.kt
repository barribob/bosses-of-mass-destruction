package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.BlockView
import net.minecraft.world.chunk.ChunkStatus
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.abs

class MobWardBlock(private val factory: (() -> BlockEntity)?, settings: Settings) : Block(settings),
    BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity? = factory?.invoke()

    companion object {
        fun canSpawn(serverWorld: ServerWorld, pos: BlockPos.Mutable, cir: CallbackInfoReturnable<Boolean>) {
            if (cir.returnValue == false) return

            val chunkPos = ChunkPos(pos)
            for (x in chunkPos.x - 4..chunkPos.x + 4) {
                for (z in chunkPos.z - 4..chunkPos.z + 4) {
                    val chunk = serverWorld.getChunk(x, z, ChunkStatus.FULL)
                    ModComponents.getChunkBlockCache(chunk).ifPresent { component ->
                        val blocks = component.getBlocksFromChunk(ModBlocks.mobWard)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.z - pos.z) < 64 }) {
                            cir.returnValue = false
                        }
                    }
                }
            }
        }
    }
}