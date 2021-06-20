package net.barribob.boss.structure

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.SimpleStructurePiece
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructurePieceType
import net.minecraft.structure.StructurePlacementData
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ServerWorldAccess
import java.util.*

/**
 * Credit: https://fabricmc.net/wiki/tutorial:structures
 */
class ModStructurePiece : SimpleStructurePiece {
    private val rot: BlockRotation

    constructor(serverWorld: ServerWorld, NbtCompound: NbtCompound, type: StructurePieceType) : super(
        type,
        NbtCompound,
        serverWorld,
        {
            StructurePlacementData()
            .setRotation(BlockRotation.valueOf(NbtCompound.getString("Rot")))
            .setMirror(BlockMirror.NONE)
            .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS) }
    ) {
        this.rot = BlockRotation.valueOf(NbtCompound.getString("Rot"))
    }

    constructor(
        structureManager: StructureManager,
        pos: BlockPos,
        template: Identifier,
        rotation: BlockRotation,
        type: StructurePieceType,
    ) : super(
        type, 0, structureManager, template, template.toString(),
        StructurePlacementData()
            .setRotation(rotation)
            .setMirror(BlockMirror.NONE)
            .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS), pos
    ) {
        this.rot = rotation
    }

    override fun writeNbt(world: ServerWorld?, nbt: NbtCompound) {
        super.writeNbt(world, nbt)
        nbt.putString("Rot", this.rot.name)
    }

    override fun handleMetadata(
        metadata: String, pos: BlockPos, serverWorldAccess: ServerWorldAccess, random: Random, boundingBox: BlockBox
    ) {

    }
}