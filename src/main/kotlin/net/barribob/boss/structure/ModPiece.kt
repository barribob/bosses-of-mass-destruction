package net.barribob.boss.structure

import net.barribob.boss.utils.ModStructures
import net.minecraft.nbt.CompoundTag
import net.minecraft.structure.SimpleStructurePiece
import net.minecraft.structure.Structure
import net.minecraft.structure.StructureManager
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
class ModPiece : SimpleStructurePiece {
    private val rot: BlockRotation
    private val template: Identifier

    constructor(structureManager: StructureManager, compoundTag: CompoundTag) : super(
        ModStructures.obsidilithArenaPiece,
        compoundTag
    ) {
        template = Identifier(compoundTag.getString("Template"))
        this.rot = BlockRotation.valueOf(compoundTag.getString("Rot"))
        initializeStructureData(structureManager)
    }

    constructor(
        structureManager: StructureManager,
        pos: BlockPos,
        template: Identifier,
        rotation: BlockRotation
    ) : super(ModStructures.obsidilithArenaPiece, 0) {
        this.pos = pos
        this.rot = rotation
        this.template = template
        initializeStructureData(structureManager)
    }

    private fun initializeStructureData(structureManager: StructureManager) {
        val structure: Structure = structureManager.getStructureOrBlank(template)
        val placementData = StructurePlacementData()
            .setRotation(this.rot)
            .setMirror(BlockMirror.NONE)
            .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS)
        setStructureData(structure, pos, placementData)
    }

    override fun toNbt(tag: CompoundTag) {
        super.toNbt(tag)
        tag.putString("Template", template.toString())
        tag.putString("Rot", this.rot.name)
    }

    override fun handleMetadata(
        metadata: String?, pos: BlockPos?, serverWorldAccess: ServerWorldAccess?, random: Random?,
        boundingBox: BlockBox?
    ) {
    }
}