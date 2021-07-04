package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendSpikePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory

class SpikeAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        placeSpikes(target)
        return 120
    }

    private fun placeSpikes(target: ServerPlayerEntity) {
        val riftBurst = Spikes(
            entity,
            target.serverWorld,
            Particles.VOID_BLOSSOM_SPIKE_INDICATOR,
            indicatorDelay,
            eventScheduler,
            {
                val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                it.damage(DamageSource.mob(entity), damage)
            }, shouldCancel
        )

        for (i in 0 until 3) {
            val timeBetweenRifts = 30
            val initialDelay = 30
            eventScheduler.addEvent(TimedEvent({
                val placement =
                    ObsidilithUtils.approximatePlayerNextPosition(ModComponents.getPlayerPositions(target), target.pos)
                target.serverWorld.playSound(
                    placement,
                    Mod.sounds.waveIndicator,
                    SoundCategory.HOSTILE,
                    1.0f,
                    range = 32.0
                )

                eventScheduler.addEvent(TimedEvent({
                    target.serverWorld.playSound(
                        placement,
                        Mod.sounds.voidBlossomSpike,
                        SoundCategory.HOSTILE,
                        1.2f,
                        range = 32.0
                    )
                }, indicatorDelay, shouldCancel = shouldCancel))

                val successfulSpikes = circlePoints.mapNotNull { riftBurst.tryPlaceRift(placement.add(it)) }

                eventScheduler.addEvent(TimedEvent({
                    entity.sendSpikePacket(successfulSpikes)
                }, indicatorDelay, shouldCancel = shouldCancel))
            }, initialDelay + i * timeBetweenRifts, shouldCancel = shouldCancel))
        }
    }

    companion object {
        const val indicatorDelay = 20
        const val maxAge = 20
    }
}