package net.barribob.boss.block.structure_repair

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.feature.StructureFeature

class GauntletStructureRepair : StructureRepair {
    override fun associatedStructure(): StructureFeature<*> = ModStructures.gauntletArenaStructure
    override fun repairStructure(world: ServerWorld, structureStart: StructureStart<*>) {
        val pos = runeCenter(structureStart)

        world.spawnParticle(Particles.GAUNTLET_REVIVE_SPARKLES, pos.up(5).asVec3d(), VecUtils.unit.multiply(3.0), 100)

        spawnBlocks(world, pos)
    }

    private fun spawnBlocks(world: ServerWorld, pos: BlockPos) {
        for (x in -1..1) {
            for (z in -1..1) {
                for (y in 0..4) {
                    world.setBlockState(pos.add(x, y, z), ModBlocks.sealedBlackstone.defaultState)
                }
            }
        }
        val up = pos.up()
        val seal = ModBlocks.gauntletBlackstone.defaultState
        world.setBlockState(up, seal)
        world.setBlockState(up.east(), seal)
        world.setBlockState(up.north(), seal)
        world.setBlockState(up.west(), seal)
        world.setBlockState(up.south(), seal)
    }

    private fun runeCenter(structureStart: StructureStart<*>): BlockPos {
        return structureStart.boundingBox.center.down(10)
    }

    override fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart<*>): Boolean {
        val pos = runeCenter(structureStart)
        val hasSealAlready = world.getBlockState(pos.up()).block == ModBlocks.gauntletBlackstone
        val noBoss = world.getEntitiesByType(Entities.GAUNTLET) { it.squaredDistanceTo(pos.asVec3d()) < 100 * 100 }.none()
        return !hasSealAlready && noBoss
    }
}