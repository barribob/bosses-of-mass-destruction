package net.barribob.boss.structure.void_blossom_cavern

import com.google.common.collect.Lists
import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IPieceGenerator
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.ChunkRandom
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.Feature
import java.util.*
import kotlin.math.sqrt

class VoidBlossomCavernPieceGenerator : IPieceGenerator {

    override fun generate(
        world: StructureWorldAccess,
        structureAccessor: StructureAccessor,
        chunkGenerator: ChunkGenerator,
        random: Random,
        boundingBox: BlockBox,
        chunkPos: ChunkPos,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ): Boolean {
        val minY = chunkGenerator.minimumY
        val caveDecorators = listOf(
            SpikeCaveDecorator(minY),
            MossFloorCaveDecorator(minY, random),
            MossCeilingCaveDecorator(minY, random),
            SporeBlossomCaveDecorator(minY, random)
        )

        generateCave(world, pos.up(17), structurePiece, random, boundingBox, caveDecorators, chunkGenerator.minimumY)

        for (decorator in caveDecorators) {
            decorator.generate(world, chunkGenerator, random, boundingBox, pos, structurePiece)
        }

        return true
    }

    private fun generateCave(
        world: StructureWorldAccess,
        pos: BlockPos,
        structurePiece: IStructurePiece,
        random: Random,
        boundingBox: BlockBox,
        caveDecorators: List<ICaveDecorator>,
        bottomOfWorld: Int
    ) {
        val noiseMultiplier = 0.005
        val outerWallDistance = UniformIntProvider.create(3, 4)
        val pointOffset = UniformIntProvider.create(1, 2)
        val minY = bottomOfWorld - pos.y
        val maxY = minY + 32
        val minXZ = -32
        val maxXZ = 32
        val verticalSquish = 2.0
        val distributionPoints = 5

        val randoms: MutableList<Pair<BlockPos, Int>> = Lists.newLinkedList()
        val chunkRandom = ChunkRandom(world.seed)
        val doublePerlinNoiseSampler = DoublePerlinNoiseSampler.create(chunkRandom, -4, 1.0)
        val d = distributionPoints.toDouble() / outerWallDistance.max.toDouble()
        val airThickness = 1.0 / sqrt(25.2 + d)
        val outerLayerThickness = 1.0 / sqrt(30.2 + d)

        var randomPos: BlockPos
        var r = 0
        while (r < distributionPoints) {
            randomPos =
                pos.add(outerWallDistance[random], outerWallDistance[random], outerWallDistance[random])
            randoms.add(
                Pair.of(
                    randomPos,
                    pointOffset[random]
                )
            )
            ++r
        }

        val predicate = Feature.notInBlockTagPredicate(BlockTags.FEATURES_CANNOT_REPLACE.id)
        val positions: Iterator<BlockPos> =
            BlockPos.iterate(pos.add(minXZ, minY, minXZ), pos.add(maxXZ, maxY, maxXZ)).iterator()
        while (true) {
            var noisedDistance: Double
            var samplePos: BlockPos
            do {
                if (!positions.hasNext()) {
                    return
                }

                samplePos = positions.next()

                val noise = doublePerlinNoiseSampler.sample(
                    samplePos.x.toDouble(),
                    samplePos.y.toDouble(),
                    samplePos.z.toDouble()
                ) * noiseMultiplier

                noisedDistance = 0.0
                val randomsIter: Iterator<Pair<BlockPos, Int>> = randoms.iterator()

                while (randomsIter.hasNext()) {
                    val pair = randomsIter.next()
                    val distancePos = BlockPos(samplePos.x, (((samplePos.y - bottomOfWorld) * verticalSquish) + bottomOfWorld).toInt(), samplePos.z)
                    noisedDistance += MathHelper.fastInverseSqrt(distancePos.getSquaredDistance(pair.first) + (pair.second).toDouble()) + noise
                }
            } while (noisedDistance < outerLayerThickness)

            val canReplace = {
                predicate.test(world.getBlockState(samplePos)) &&
                        !world.getBlockState(samplePos).isAir &&
                        world.getBlockState(samplePos).block != Blocks.BEDROCK &&
                        samplePos.y > 4 + bottomOfWorld
            }

            if (noisedDistance >= airThickness) {
                replaceBlock(
                    canReplace,
                    world,
                    samplePos,
                    structurePiece,
                    boundingBox,
                    Blocks.AIR.defaultState,
                    caveDecorators
                )
            } else if (noisedDistance >= outerLayerThickness) {
                replaceBlock(
                    canReplace,
                    world,
                    samplePos,
                    structurePiece,
                    boundingBox,
                    if(samplePos.y > 0) Blocks.STONE.defaultState else Blocks.DEEPSLATE.defaultState,
                    caveDecorators
                )
            }
        }
    }

    private fun replaceBlock(
        predicate: () -> Boolean,
        world: StructureWorldAccess,
        samplePos: BlockPos,
        structurePiece: IStructurePiece,
        boundingBox: BlockBox,
        blockState: BlockState,
        caveDecorators: List<ICaveDecorator>
    ) {
        if (predicate()) structurePiece.addBlock(world, blockState, samplePos, boundingBox)

        for (decorator in caveDecorators) {
            decorator.onBlockPlaced(BlockPos(samplePos), blockState.block)
        }
    }
}