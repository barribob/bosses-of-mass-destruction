package net.barribob.boss.block.structure_repair

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.structure.void_blossom_cavern.BossBlockDecorator
import net.barribob.boss.utils.ModStructures
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.feature.StructureFeature

class VoidBlossomStructureRepair : StructureRepair {
    override fun associatedStructure(): StructureFeature<*> = ModStructures.voidBlossomArenaStructure
    override fun repairStructure(world: ServerWorld, structureStart: StructureStart<*>) {
        val offset = getCenterSpawn(structureStart, world)
        world.setBlockState(offset, ModBlocks.voidBlossomSummonBlock.defaultState)
    }

    override fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart<*>): Boolean {
        val centerPos = getCenterSpawn(structureStart, world)
        return world.getEntitiesByType(Entities.VOID_BLOSSOM) { it.squaredDistanceTo(centerPos.asVec3d()) < 100 * 100 }.none()
    }

    private fun getCenterSpawn(
        structureStart: StructureStart<*>,
        world: ServerWorld
    ): BlockPos {
        val pos = structureStart.boundingBox.center
        return BossBlockDecorator.bossBlockOffset(pos, world.bottomY)
    }
}