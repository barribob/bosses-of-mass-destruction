package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.intprovider.ConstantIntProvider
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.blockpredicate.BlockPredicate
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.decorator.*
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider
import java.util.*

class SporeBlossomCaveDecorator(private val bottomOfWorld: Int, private val random: Random) : ICaveDecorator {
    private val sporeBlossomPositions = mutableListOf<BlockPos>()
    private val sporeBlossom =
        (Feature.SIMPLE_BLOCK.configure(SimpleBlockFeatureConfig(SimpleBlockStateProvider.of(Blocks.SPORE_BLOSSOM)))
            .withPlacement(
                CountPlacementModifier.of(25),
                SquarePlacementModifier.of(),
                PlacedFeatures.BOTTOM_TO_120_RANGE,
                EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12),
                RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)),
                BiomePlacementModifier.of()
            ));

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y > 20 + bottomOfWorld && random.nextInt(20) == 0 && block != Blocks.AIR) {
            sporeBlossomPositions.add(pos)
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
        val spacedSporeBlossomPositions =
            sporeBlossomPositions.groupBy { Pair(it.x shr 3, it.z shr 3) }.map { it.value.first() }
        for (sporePos in spacedSporeBlossomPositions) {
            if (boundingBox.contains(sporePos)) {
                sporeBlossom.generate(world, chunkGenerator, random, sporePos)
            }
        }
    }
}