package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.datafixers.util.Pair
import net.barribob.boss.structure.util.IStructurePiece
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator
import kotlin.math.pow

class SpikeCaveDecorator(private val bottomOfWorld: Int, private val random: Random) : ICaveDecorator {
    private val spikePositions = mutableListOf<BlockPos>()
    private val baseBlocks = MathUtils.buildBlockCircle(4.2)

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        val spikeSpacing = (random.nextInt(20) + 10).toDouble().pow(2)
        if (pos.y == 5 + bottomOfWorld && block != Blocks.AIR && spikePositions.all { it.getSquaredDistance(pos) > spikeSpacing }) {
            spikePositions.add(pos)
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
        val spacesSpikePositions = spikePositions.groupBy { Pair(it.x shr 2, it.z shr 2) }.map { it.value.first() }
        for (outerPos in spacesSpikePositions) {
            val centerDirection = pos.subtract(outerPos).asVec3d().planeProject(VecUtils.yAxis).normalize()
            val tip = centerDirection
                .multiply((5 + random.nextInt(3)).toDouble())
                .add(VecUtils.yAxis.multiply((7 + random.nextInt(5)).toDouble()))
            generateSpike(outerPos, outerPos.add(BlockPos.ofFloored(tip)), structurePiece, world, boundingBox, centerDirection)
        }
    }

    private fun generateSpike(
        origin: BlockPos,
        tip: BlockPos,
        structurePiece: IStructurePiece,
        world: StructureWorldAccess,
        boundingBox: BlockBox,
        centerDirection: Vec3d
    ) {
        val centerDirectionPos = centerDirection.multiply(3.0)
        val blockSet = baseBlocks.map{BlockPos.ofFloored(it)}.toSet()
        val innerBlockSet = blockSet.filter { it.getSquaredDistance(centerDirectionPos) < 2.0.pow(2) }.toSet()
        val middleBlockSet = blockSet.subtract(innerBlockSet).filter { it.getSquaredDistance(centerDirectionPos) < 3.7.pow(2) }.toSet()
        val outerBlockSet = blockSet.subtract(middleBlockSet).subtract(innerBlockSet)

        for (block in innerBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.add(origin), tip)
            for (pos in blocksInLine) {
                val blockState = if(random.nextInt(16) == 0) Blocks.BUDDING_AMETHYST.defaultState else Blocks.AMETHYST_BLOCK.defaultState
                structurePiece.addBlock(world, blockState, pos, boundingBox)
            }
        }
        for (block in middleBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.add(origin), tip)
            for (pos in blocksInLine) {
                structurePiece.addBlock(world, Blocks.CALCITE.defaultState, pos, boundingBox)
            }
        }
        for (block in outerBlockSet) {
            val blocksInLine = MathUtils.getBlocksInLine(block.add(origin), tip)
            for (pos in blocksInLine) {
                structurePiece.addBlock(world, Blocks.SMOOTH_BASALT.defaultState, pos, boundingBox)
            }
        }
    }
}