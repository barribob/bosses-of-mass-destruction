package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendSpikePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class SpikeWaveAction(val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler, private val shouldCancel: () -> Boolean) : IActionWithCooldown {
    private val firstCirclePoints = MathUtils.buildBlockCircle(7.0)
    private val secondCirclePoints = MathUtils.buildBlockCircle(14.0).subtract(firstCirclePoints).toList()
    private val thirdCirclePoints = MathUtils.buildBlockCircle(21.0).subtract(secondCirclePoints).subtract(firstCirclePoints).toList()

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        placeRifts(target)
        return 120
    }

    private fun placeRifts(target: ServerPlayerEntity) {
        val secondBurstDelay = 30
        val thirdBurstDelay = 60
        val world = target.serverWorld
        val spikeGenerator = Spikes(
            entity,
            world,
            Particles.VOID_BLOSSOM_SPIKE_WAVE_INDICATOR,
            indicatorDelay,
            eventScheduler,
            {
                val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                it.damage(DamageSource.mob(entity), damage)
            }, shouldCancel
        )

        world.playSound(entity.pos, Mod.sounds.waveIndicator, SoundCategory.HOSTILE, 3.0f, 0.7f, 64.0)

        createBurst(world, spikeGenerator, firstCirclePoints)
        eventScheduler.addEvent(TimedEvent({
            createBurst(world, spikeGenerator, secondCirclePoints)
        }, secondBurstDelay, shouldCancel = shouldCancel))
        eventScheduler.addEvent(TimedEvent({
            createBurst(world, spikeGenerator, thirdCirclePoints)
        }, thirdBurstDelay, shouldCancel = shouldCancel))
    }

    private fun createBurst(
        world: ServerWorld,
        spikeGenerator: Spikes,
        positions: List<Vec3d>
    ) {
        eventScheduler.addEvent(TimedEvent({
            world.playSound(entity.pos, Mod.sounds.voidBlossomSpike, SoundCategory.HOSTILE, 1.2f, range = 64.0)
        }, indicatorDelay, shouldCancel = shouldCancel))

        val placedPositions = positions.mapNotNull { spikeGenerator.tryPlaceRift(entity.pos.add(it)) }.toList()
        eventScheduler.addEvent(TimedEvent({
            entity.sendSpikePacket(placedPositions)
        }, indicatorDelay, shouldCancel = shouldCancel))
    }

    companion object {
        const val indicatorDelay = 30
    }
}