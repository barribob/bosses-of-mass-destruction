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
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator
import java.util.*

class SpikeCaveDecorator(private val bottomOfWorld: Int) : ICaveDecorator {
    private val spikePositions = mutableListOf<BlockPos>()
    private var blocksSinceLastSpike = 0
    private val blocks = MathUtils.buildBlockCircle(3.0)

    override fun onBlockPlaced(pos: BlockPos, block: Block) {
        if (pos.y == 5 + bottomOfWorld && block != Blocks.AIR) {
            if (blocksSinceLastSpike == 20) {
                spikePositions.add(pos)
                blocksSinceLastSpike = 0
            } else {
                blocksSinceLastSpike++
            }
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
            val tip = pos.subtract(outerPos).asVec3d().planeProject(VecUtils.yAxis).normalize()
                .multiply((5 + random.nextInt(3)).toDouble())
                .add(VecUtils.yAxis.multiply((7 + random.nextInt(5)).toDouble()))
            generateSpike(outerPos, outerPos.add(BlockPos(tip)), structurePiece, world, boundingBox)
        }
    }

    private fun generateSpike(
        origin: BlockPos,
        tip: BlockPos,
        structurePiece: IStructurePiece,
        world: StructureWorldAccess,
        boundingBox: BlockBox,
    ) {
        for (block in blocks) {
            val blocksInLine = MathUtils.getBlocksInLine(BlockPos(block.add(origin.asVec3d())), tip)
            for (pos in blocksInLine) {
                structurePiece.addBlock(world, Blocks.BEDROCK.defaultState, pos, boundingBox)
            }
        }
    }
}