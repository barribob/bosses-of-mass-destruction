package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity.Companion.isEnergized
import net.barribob.boss.mob.utils.ITrackedDataHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.Event
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.rotateVector
import net.minecraft.entity.data.TrackedData

class GauntletClientEnergyShieldHandler(
    private val entity: GauntletEntity,
    private val eventScheduler: EventScheduler
) : ITrackedDataHandler {
    private var energizedRenderAlpha = 0.0f
    private val energizedParticles = ClientParticleBuilder(Particles.SPARKLES)
        .brightness(Particles.FULL_BRIGHT)
        .color(ModColors.LASER_RED)
        .colorVariation(0.2)
        .scale(0.25f)

    fun getRenderAlpha() = energizedRenderAlpha

    override fun onTrackedDataSet(data: TrackedData<*>) {
        if (isEnergized == data && entity.dataTracker.get(isEnergized) && entity.world.isClient) {
            eventScheduler.addEvent(TimedEvent({ energizedRenderAlpha += 0.1f }, 0, 10))
            eventScheduler.addEvent(
                Event(
                    { true },
                    ::spawnParticles,
                    { !entity.isAlive || !entity.dataTracker.get(isEnergized) })
            )
        } else {
            energizedRenderAlpha = 0.0f
        }
    }

    private fun spawnParticles() {
        val look = entity.rotationVector
        val cross = look.crossProduct(VecUtils.yAxis)
        val rotatedOffset = cross.rotateVector(look, RandomUtils.range(0, 359).toDouble())
        val particlePos = entity.eyePos().add(rotatedOffset)
        val particleVel = rotatedOffset.rotateVector(look, 90.0).multiply(0.1)
        energizedParticles.build(particlePos, particleVel)
    }
}