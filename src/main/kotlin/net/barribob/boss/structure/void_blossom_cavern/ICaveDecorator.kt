package net.barribob.boss.structure.void_blossom_cavern

import net.barribob.boss.structure.util.IStructurePiece
import net.minecraft.block.Block
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator

interface ICaveDecorator {
    fun onBlockPlaced(pos: BlockPos, block: Block)
    fun generate(world: StructureWorldAccess,
                 chunkGenerator: ChunkGenerator,
                 random: Random,
                 boundingBox: BlockBox,
                 pos: BlockPos,
                 structurePiece: IStructurePiece
    )
}