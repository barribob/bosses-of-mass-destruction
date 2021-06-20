package net.barribob.boss.structure.util

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructurePiece
import net.minecraft.structure.StructurePieceType
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.ChunkGenerator
import java.util.*

class CodeStructurePiece : StructurePiece, IStructurePiece {
    private val pieceGenerator: IPieceGenerator

    constructor(type: StructurePieceType, boundingBox: BlockBox, structurePieceData: IPieceGenerator) : super(type, 0, boundingBox) {
        this.pieceGenerator = structurePieceData
    }

    constructor(type: StructurePieceType, nbt: NbtCompound, structurePieceData: IPieceGenerator) : super(type, nbt) {
        this.pieceGenerator = structurePieceData
    }

    override fun writeNbt(world: ServerWorld?, nbt: NbtCompound?) {
    }

    override fun generate(
        world: StructureWorldAccess,
        structureAccessor: StructureAccessor,
        chunkGenerator: ChunkGenerator,
        random: Random,
        boundingBox: BlockBox,
        chunkPos: ChunkPos,
        pos: BlockPos
    ): Boolean {
        return pieceGenerator.generate(
            world,
            structureAccessor,
            chunkGenerator,
            Random(world.seed),
            boundingBox,
            chunkPos,
            pos,
            this
        )
    }

    override fun addBlock(world: StructureWorldAccess, block: BlockState, pos: BlockPos, box: BlockBox) {
        super.addBlock(world, block, pos.x, pos.y, pos.z, box)
    }
}