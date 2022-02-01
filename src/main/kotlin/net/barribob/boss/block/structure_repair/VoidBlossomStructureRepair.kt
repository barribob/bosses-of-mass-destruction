package net.barribob.boss.block.structure_repair

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.structure.void_blossom_cavern.BossBlockDecorator
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.NetworkUtils
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.client.world.ClientWorld
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.gen.feature.StructureFeature

class VoidBlossomStructureRepair : StructureRepair {
    override fun associatedStructure(): StructureFeature<*> = ModStructures.voidBlossomArenaStructure
    override fun repairStructure(world: ServerWorld, structureStart: StructureStart<*>) {
        val offset = getCenterSpawn(structureStart, world)
        NetworkUtils.sendVoidBlossomRevivePacket(world, offset.asVec3d())

        ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
            world.setBlockState(offset, ModBlocks.voidBlossomSummonBlock.defaultState)
        }, 60))
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

    companion object {
        private val spikeParticleFactory = ClientParticleBuilder(Particles.SPARKLES)
            .color(ModColors.VOID_PURPLE)
            .colorVariation(0.25)
            .brightness(Particles.FULL_BRIGHT)
            .scale { 0.5f * (1 - it * 0.25f) }
            .age(20)

        fun handleVoidBlossomRevivePacket(pos: Vec3d, world: ClientWorld) {
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                ModUtils.spawnRotatingParticles(
                    ModUtils.RotatingParticles(
                        pos.add(VecUtils.yAxis.multiply(RandomUtils.range(1.0, 10.0))),
                        spikeParticleFactory,
                        1.0,
                        2.0,
                        3.0,
                        4.0
                    )
                )
            }, 0, 50))

            MathUtils.buildBlockCircle(2.3).forEach{
                ClientParticleBuilder(Particles.VOID_BLOSSOM_SPIKE_INDICATOR)
                    .age(60)
                    .build(pos.add(0.0, 0.1, 0.0).add(it))
            }
        }
    }
}