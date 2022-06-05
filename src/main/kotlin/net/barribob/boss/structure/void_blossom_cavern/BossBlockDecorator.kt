package net.barribob.boss.structure.void_blossom_cavern

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.Block
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator

class BossBlockDecorator(private val bottomOfWorld: Int) : ICaveDecorator {
    override fun onBlockPlaced(pos: BlockPos, block: Block) {
    }

    override fun generate(
        world: StructureWorldAccess,
        chunkGenerator: ChunkGenerator,
        random: Random,
        boundingBox: BlockBox,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ) {
        structurePiece.addBlock(world, ModBlocks.voidBlossomSummonBlock.defaultState, bossBlockOffset(pos, bottomOfWorld), boundingBox)
    }

    companion object {
        fun bossBlockOffset(pos: BlockPos, bottomOfWorld: Int) = BlockPos(pos.x + 3, bottomOfWorld + 5, pos.z + 3)
    }
}