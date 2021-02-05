package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class WaveAction(val entity: LivingEntity, private val status: Byte, private val directionProvider: () -> Vec3d) : IActionWithCooldown {
    private val riftRadius = 4
    private val circlePoints = MathUtils.buildBlockCircle(riftRadius)
    private val world = entity.world
    private val eventScheduler = ModComponents.getWorldEventScheduler(world)

    override fun perform(): Int {
        world.sendEntityStatus(
            entity,
            status
        ) // Todo: if all statuses are just sent instantly, we can move this up to the entity
        placeRifts()
        return 80
    }

    private fun placeRifts() {
        val riftBurst = RiftBurst(
            entity,
            world as ServerWorld,
            Particles.OBSIDILITH_WAVE_INDICATOR,
            Particles.OBSIDILITH_WAVE,
            waveDelay,
            eventScheduler
        )
        world.playSound(entity.pos, Mod.sounds.teleportPrepare, SoundCategory.HOSTILE, 0.7f, range = 32.0)
        eventScheduler.addEvent(TimedEvent({
            val direction = directionProvider().normalize().multiply(riftRadius.toDouble())
            val numRifts = 5
            val startRiftPos = entity.pos.add(direction)
            val endRiftPos = startRiftPos.add(direction.multiply(numRifts.toDouble() * 1.5))
            MathUtils.lineCallback(startRiftPos, endRiftPos, numRifts) { linePos, i ->
                eventScheduler.addEvent(TimedEvent({
                    world.playSound(linePos, Mod.sounds.missilePrepare, SoundCategory.HOSTILE, 0.7f, range = 32.0)
                    eventScheduler.addEvent(TimedEvent({
                        world.playSound(linePos, Mod.sounds.obsidilithBurst, SoundCategory.HOSTILE, 1.2f, range = 32.0)
                    }, waveDelay, shouldCancel = { !entity.isAlive }))

                    for(point in circlePoints) {
                        riftBurst.tryPlaceRift(linePos.add(point))
                    }
                },  i * 8, shouldCancel = { !entity.isAlive }))
            }
        }, attackStartDelay, shouldCancel = { !entity.isAlive }))
    }

    companion object {
        const val waveDelay = 20
        const val attackStartDelay = 20
    }
}