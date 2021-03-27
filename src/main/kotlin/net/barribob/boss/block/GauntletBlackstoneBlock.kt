package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.util.*

class GauntletBlackstoneBlock(settings: Settings) : Block(settings) {
    val laserChargeParticles = ClientParticleBuilder(Particles.SPARKLES)
        .brightness(Particles.FULL_BRIGHT)
        .color(ModColors.LASER_RED)
        .colorVariation(0.2)

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {

        if(world.isClient) return

        for (dir in listOf(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
            val centerPos = pos.add(dir.vector)
            val centerBlock = world.getBlockState(centerPos).block
            if (centerBlock == this) {
                spawnGauntlet(centerPos, world)
                break
            }
        }
        super.onBreak(world, pos, state, player)
    }

    private fun spawnGauntlet(centerPos: BlockPos, world: World) {
        val spawnPos = centerPos.asVec3d().add(Vec3d(0.5, -0.5, 0.5))
        val entity = Entities.GAUNTLET.create(world)
        entity!!.updatePosition(spawnPos.x, spawnPos.y, spawnPos.z)
        world.spawnEntity(entity)

        val eventScheduler = ModComponents.getWorldEventScheduler(world)

        for (y in -1..4) {
            eventScheduler.addEvent(TimedEvent({
                for (x in -1..1) {
                    for (z in -1..1) {
                        world.breakBlock(centerPos.add(Vec3i(x, y, z)), false)
                    }
                }
            }, 10 + y * 5))
        }
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        val particlePos = pos.asVec3d().add(VecUtils.unit.multiply(0.5)).add(RandomUtils.randVec().normalize())
        laserChargeParticles.build(particlePos)
    }
}