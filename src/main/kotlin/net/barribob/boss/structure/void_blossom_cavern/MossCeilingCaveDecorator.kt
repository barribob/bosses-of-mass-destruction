package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.UndergroundConfiguredFeatures
import java.util.*

class MossCeilingCaveDecorator(private val bottomOfWorld: Int, private val random: Random) : ICaveDecorator {
    private val mossCeilingPositions = mutableListOf<BlockPos>()

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y > 18 + bottomOfWorld && random.nextInt(20) == 0 && block != Blocks.AIR) {
            mossCeilingPositions.add(pos)
        }
    }

    override fun generate(
        world: StructureWorldAccess,
        chunkGenerator: ChunkGenerator,
        random: Random,
        boundingBox: BlockBox,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ) {
        val spacedMossCeilingPositions =
            mossCeilingPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (mossPos in spacedMossCeilingPositions) {
            if (boundingBox.contains(mossPos)) {
                UndergroundConfiguredFeatures.MOSS_PATCH_CEILING.value().generate(world, chunkGenerator, random, mossPos)
            }
        }
    }
}