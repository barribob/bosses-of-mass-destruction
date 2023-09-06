package net.barribob.boss.block.structure_repair

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithEffectHandler
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.client.world.ClientWorld
import net.minecraft.registry.RegistryKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.gen.structure.Structure

class ObsidilithStructureRepair : StructureRepair {
    override fun associatedStructure(): RegistryKey<Structure> = Mod.structures.obsidilithStructureRegistry.configuredStructureKey

    override fun repairStructure(world: ServerWorld, structureStart: StructureStart) {
        val topCenter = getTopCenter(structureStart)
        val worldEventScheduler = ModComponents.getWorldEventScheduler(world)
        NetworkUtils.sendObsidilithRevivePacket(world, topCenter.asVec3d().add(0.5, 0.5, 0.5))

        for (y in 0..ObsidilithUtils.deathPillarHeight) {
            worldEventScheduler.addEvent(TimedEvent({
                world.playSound(null, topCenter, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0f, world.random.randomPitch())
                for (pos in ObsidilithUtils.circlePos) {
                    world.removeBlock(BlockPos(pos.x.toInt(), y, pos.z.toInt()).add(topCenter), false)
                }
                if(y == 0) {
                    world.setBlockState(topCenter, ModBlocks.obsidilithSummonBlock.defaultState)
                }
            }, y * ObsidilithUtils.ticksBetweenPillarLayer))
        }
    }

    private fun getTopCenter(structureStart: StructureStart): BlockPos {
        val centerPos = structureStart.boundingBox.center
        return BlockPos(centerPos.x, structureStart.boundingBox.maxY, centerPos.z)
    }

    override fun shouldRepairStructure(world: ServerWorld, structureStart: StructureStart): Boolean {
        val topCenter = getTopCenter(structureStart)
        val noBoss = world.getEntitiesByType(Entities.OBSIDILITH) { it.squaredDistanceTo(topCenter.asVec3d()) < 100 * 100 }.none()
        val hasAltar = world.getBlockState(topCenter).block == ModBlocks.obsidilithSummonBlock
        return noBoss && !hasAltar
    }

    companion object {
        fun handleObsidilithRevivePacket(pos: Vec3d, world: ClientWorld) {
            ObsidilithEffectHandler.spawnPillarParticles(pos, ModComponents.getWorldEventScheduler(world))
        }
    }
}