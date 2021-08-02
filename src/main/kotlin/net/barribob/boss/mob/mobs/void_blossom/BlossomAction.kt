package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.mob.mobs.void_blossom.hitbox.NetworkedHitboxManager
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendPlacePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos

class BlossomAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val blossomPositions = listOf(
        VecUtils.xAxis,
        VecUtils.zAxis,
        VecUtils.xAxis.negate(),
        VecUtils.zAxis.negate(),
        VecUtils.xAxis.add(VecUtils.zAxis),
        VecUtils.xAxis.add(VecUtils.zAxis.negate()),
        VecUtils.xAxis.negate().add(VecUtils.zAxis),
        VecUtils.xAxis.negate().add(VecUtils.zAxis.negate())
    ).map { it.normalize().multiply(15.0) }

    override fun perform(): Int {
        val serverWorld = entity.world
        if (serverWorld !is ServerWorld) return 80

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.dataTracker.set(NetworkedHitboxManager.hitbox, HitboxId.SpikeWave3.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.dataTracker.set(NetworkedHitboxManager.hitbox, HitboxId.Idle.id)
                }, 80)
            )
        )

        placeBlossoms(serverWorld)
        return 120
    }

    private fun placeBlossoms(world: ServerWorld) {
        val positions = blossomPositions.map { BlockPos(it.add(entity.pos)) }.shuffled()
        val hpRatio = entity.health / entity.maxHealth
        val protectedPositions = if(hpRatio < entity.hpMilestones[1]) 6 else if (hpRatio < entity.hpMilestones[2]) 3 else 0

        world.playSound(entity.pos, Mod.sounds.waveIndicator, SoundCategory.HOSTILE, 2.0f, 0.7f, 64.0)

        for(i in 0 until 8) {
            eventScheduler.addEvent(TimedEvent({
                val blossomPos = positions[i]
                world.setBlockState(blossomPos, ModBlocks.voidBlossom.defaultState)
                entity.sendPlacePacket(blossomPos.asVec3d().add(VecUtils.unit.multiply(0.5)))
                world.playSound(blossomPos.asVec3d(), Mod.sounds.petalBlade, SoundCategory.HOSTILE, 1.0f, entity.random.randomPitch(), 64.0)

                if(i < protectedPositions) {
                    for (x in -1..1) {
                        for (z in -1..1) {
                            for (y in 0..2) {
                                if ((x != 0 || z != 0)) {
                                    world.setBlockState(blossomPos.add(x, y, z), ModBlocks.vineWall.defaultState)
                                }
                            }
                        }
                    }
                }

            },40 + i * 8, shouldCancel = shouldCancel))
        }
    }
}