package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class PillarAction(val entity: LivingEntity) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(entity.world)

    override fun perform(): Int {
        val serverWorld = entity.world
        if(serverWorld !is ServerWorld) return 80
        val pillarPositions = getPillarPositions()

        pillarPositions.forEach {
            MathUtils.lineCallback(entity.eyePos(), it.asVec3d().add(0.5, 0.5, 0.5), pillarXzDistance.toInt()) { pos, _ ->
                serverWorld.spawnParticle(Particles.PILLAR_SPAWN_INDICATOR_2, pos, Vec3d.ZERO)
            }
            serverWorld.spawnParticle(Particles.PILLAR_SPAWN_INDICATOR, it.up(5).asVec3d(), Vec3d(0.3, 3.0, 0.3), 20)
        }

        eventScheduler.addEvent(TimedEvent({
            pillarPositions.forEach (::buildPillar)
        }, pillarDelay, shouldCancel = { !entity.isAlive }))
        return 100
    }

    private fun getPillarPositions(): List<BlockPos> {
        val numPillars = 4
        val pillars = mutableListOf<BlockPos>()

        for (i in 0 until numPillars) {
            val position = RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().multiply(pillarXzDistance).add(entity.pos)
            val up = entity.world.findGroundBelow(BlockPos(position).up(14))
            val ground = entity.world.findGroundBelow(up)

            if (up.y - ground.y > maxYDistance) continue

            entity.world.playSound(ground.asVec3d(), SoundEvents.BLOCK_BASALT_PLACE, SoundCategory.HOSTILE, 1.0f)
            pillars.add(ground)
        }

        return pillars
    }

    private fun buildPillar(pos: BlockPos) {
        val pillarHeight = RandomUtils.range(2, 5)
        for (i in 0 until pillarHeight) {
            entity.world.setBlockState(pos.up(i), Blocks.OBSIDIAN.defaultState)
        }
        val pillarTop = pos.up(pillarHeight)
        entity.world.setBlockState(pillarTop, ModBlocks.obsidilithRune.defaultState)
    }

    companion object {
        const val pillarXzDistance = 15.0
        const val maxYDistance = 15.0
        const val pillarDelay = 40
    }
}