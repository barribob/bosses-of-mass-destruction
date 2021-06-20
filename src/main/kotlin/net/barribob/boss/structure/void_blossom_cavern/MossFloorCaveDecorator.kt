package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.Block
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.util.FeatureContext
import java.util.*

class MossFloorCaveDecorator(private val bottomOfWorld: Int, private val random: Random) : ICaveDecorator {
    private val mossFloorPositions = mutableListOf<BlockPos>()

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y == 4 + bottomOfWorld && random.nextInt(80) == 0) {
            mossFloorPositions.add(pos)
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
        val spacedMossPositions = mossFloorPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (mossPos in spacedMossPositions) {
            if (boundingBox.contains(mossPos)) {
                Feature.VEGETATION_PATCH.generate(
                    FeatureContext(
                        world,
                        chunkGenerator,
                        random,
                        mossPos,
                        ConfiguredFeatures.MOSS_PATCH.getConfig()
                    )
                )
            }
        }
    }
}