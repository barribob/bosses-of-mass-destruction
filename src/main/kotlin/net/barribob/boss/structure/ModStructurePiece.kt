package net.barribob.boss.structure

import net.minecraft.nbt.NbtCompound
import net.minecraft.structure.*
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.ServerWorldAccess

/**
 * Credit: https://fabricmc.net/wiki/tutorial:structures
 */
class ModStructurePiece : SimpleStructurePiece {
    private val rot: BlockRotation

    constructor(manager: StructureManager, NbtCompound: NbtCompound, type: StructurePieceType) : super(
        type,
        NbtCompound,
        manager,
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

    override fun writeNbt(context: StructureContext?, nbt: NbtCompound) {
        super.writeNbt(context, nbt)
        nbt.putString("Rot", this.rot.name)
    }

    override fun handleMetadata(metadata: String?, pos: BlockPos?, world: ServerWorldAccess?, random: Random?, boundingBox: BlockBox?) {
    }
}