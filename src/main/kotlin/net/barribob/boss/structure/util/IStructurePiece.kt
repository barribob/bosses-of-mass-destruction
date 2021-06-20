package net.barribob.boss.structure.util

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.StructureWorldAccess

interface IStructurePiece {
    fun addBlock(world: StructureWorldAccess, block: BlockState, pos: BlockPos, box: BlockBox)
}