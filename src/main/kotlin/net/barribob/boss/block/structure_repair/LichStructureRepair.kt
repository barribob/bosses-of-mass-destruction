package net.barribob.boss.block.structure_repair

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Blocks
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.feature.ConfiguredStructureFeature

class LichStructureRepair : StructureRepair {
    override fun associatedStructure(): ConfiguredStructureFeature<*, *> = ModStructures.configuredLichTowerStructure

    override fun repairStructure(world: ServerWorld, structureStart: StructureStart) {
        val pos = altarCenter(world, structureStart)

        val altar = ModBlocks.chiseledStoneAltar.defaultState
        val positions = listOf(pos.west(6), pos.east(6), pos.north(6), pos.south(6))

        for(altarPos in positions) {
            world.setBlockState(altarPos, altar)
            world.spawnParticle(Particles.SOUL_FLAME, altarPos.asVec3d().add(0.5, 1.0, 0.5), VecUtils.unit, 10)
        }
    }

    private fun altarCenter(world: ServerWorld, structureStart: StructureStart): BlockPos {
        val boundingBox = structureStart.boundingBox
        val yPos = boundingBox.center.down(16).y
        val centerX = boundingBox.center.x
        val centerZ = boundingBox.center.z
        val gridPos = (-2..2).flatMap { x -> (-2..2).map { z -> Pair(x + centerX, z + centerZ) } }
            .maxByOrNull { xzPair -> countChestsInColumn(boundingBox, world, xzPair)
        }
        return BlockPos(gridPos!!.first, yPos, gridPos.second)
    }

    private fun countChestsInColumn(
        boundingBox: BlockBox,
        world: ServerWorld,
        xzPair: Pair<Int, Int>,
    ) = (boundingBox.minY..boundingBox.maxY).count {
        world.getBlockState(
            BlockPos(
                xzPair.first,
                it,
                xzPair.second
            )
        ).block == Blocks.CHEST
    }

    override fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart): Boolean {
        val pos = altarCenter(world, structureStart)
        val hasAltar = world.getBlockState(pos.west(6)).block == ModBlocks.chiseledStoneAltar
        val noBoss = world.getEntitiesByType(Entities.LICH) { it.squaredDistanceTo(pos.asVec3d()) < 100 * 100 }.none()
        return !hasAltar && noBoss
    }
}