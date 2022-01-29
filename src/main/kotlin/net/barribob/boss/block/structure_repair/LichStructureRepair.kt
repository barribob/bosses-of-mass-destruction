package net.barribob.boss.block.structure_repair

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.utils.ModStructures
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.feature.StructureFeature

class LichStructureRepair : StructureRepair {
    override fun associatedStructure(): StructureFeature<*> = ModStructures.lichTowerStructure

    override fun repairStructure(world: ServerWorld, structureStart: StructureStart<*>) {
        val pos = altarCenter(structureStart)

        val altar = ModBlocks.chiseledStoneAltar.defaultState
        world.setBlockState(pos.west(6), altar)
        world.setBlockState(pos.east(6), altar)
        world.setBlockState(pos.north(6), altar)
        world.setBlockState(pos.south(6), altar)

        MaelstromMod.debugPoints.drawDebugPoints((0..20).map { pos.asVec3d().add(RandomUtils.randVec()) }, 40, pos.asVec3d(), world)
    }

    private fun altarCenter(structureStart: StructureStart<*>): BlockPos {
        return structureStart.boundingBox.center.down(16).west(2)
    }

    override fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart<*>): Boolean {
        val pos = altarCenter(structureStart)
        val hasAltar = world.getBlockState(pos.west(6)).block == ModBlocks.chiseledStoneAltar
        val noBoss = world.getEntitiesByType(Entities.LICH) { it.squaredDistanceTo(pos.asVec3d()) < 100 * 100 }.none()
        return !hasAltar && noBoss
    }
}