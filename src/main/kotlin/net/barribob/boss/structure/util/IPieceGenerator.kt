package net.barribob.boss.structure.util

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.ChunkGenerator

interface IPieceGenerator {
    fun generate(
        world: StructureWorldAccess,
        structureAccessor: StructureAccessor,
        chunkGenerator: ChunkGenerator,
        random: Random,
        boundingBox: BlockBox,
        chunkPos: ChunkPos,
        pos: BlockPos,
        structurePiece: IStructurePiece
    ): Boolean
}