package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendSpikePacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class SpikeWaveAction(
    val entity: VoidBlossomEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val firstCirclePoints = MathUtils.buildBlockCircle(7.0)
    private val secondRadius = 14.0
    private val secondCirclePoints = MathUtils.buildBlockCircle(secondRadius).subtract(firstCirclePoints).toList()
    private val thirdRadius = 21.0
    private val thirdCirclePoints =
        MathUtils.buildBlockCircle(thirdRadius).subtract(secondCirclePoints).subtract(firstCirclePoints).toList()

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        placeRifts(target)
        return 120
    }

    private fun placeRifts(target: ServerPlayerEntity) {
        val firstBurstDelay = 20
        val secondBurstDelay = 45
        val thirdBurstDelay = 70
        val world = target.serverWorld

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave1.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave2.id)
                }, 26, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.SpikeWave3.id)
                }, 26, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 26)
            )
        )

        val spikeGenerator = Spikes(
            entity,
            world,
            Particles.VOID_BLOSSOM_SPIKE_WAVE_INDICATOR,
            indicatorDelay,
            eventScheduler,
            shouldCancel
        )

        createSpikeWave(
            {
                createBurst(spikeGenerator, firstCirclePoints)
                world.playSound(entity.pos, Mod.sounds.spikeWaveIndicator, SoundCategory.HOSTILE, 2.0f, 0.7f, 64.0)
            },
            { world.playSound(entity.pos, Mod.sounds.voidBlossomSpike, SoundCategory.HOSTILE, 1.2f, range = 64.0) },
            firstBurstDelay
        )
        createSpikeWave(
            {
                createBurst(spikeGenerator, secondCirclePoints)
                playSoundsInRadius(world, secondRadius, Mod.sounds.spikeWaveIndicator, 2.0f, 0.7f)
            },
            { playSoundsInRadius(world, secondRadius, Mod.sounds.voidBlossomSpike, 1.2f, entity.random.randomPitch()) },
            secondBurstDelay
        )
        createSpikeWave(
            {
                createBurst(spikeGenerator, thirdCirclePoints)
                playSoundsInRadius(world, thirdRadius, Mod.sounds.spikeWaveIndicator, 2.0f, 0.7f)
            },
            { playSoundsInRadius(world, thirdRadius, Mod.sounds.voidBlossomSpike, 1.2f, entity.random.randomPitch()) },
            thirdBurstDelay
        )
    }

    private fun createSpikeWave(
        indicationStageHandler: () -> Unit,
        spikeStageHandler: () -> Unit,
        burstDelay: Int
    ) {
        eventScheduler.addEvent(TimedEvent({
            indicationStageHandler()
            eventScheduler.addEvent(TimedEvent(spikeStageHandler, indicatorDelay, shouldCancel = shouldCancel))
        }, burstDelay, shouldCancel = shouldCancel))
    }

    private fun playSoundsInRadius(
        world: ServerWorld,
        radius: Double,
        soundEvent: SoundEvent,
        volume: Float,
        pitch: Float
    ) {
        for (dir in Direction.Type.HORIZONTAL) {
            val pos = entity.pos.add(Vec3d(dir.unitVector).multiply(radius))
            world.playSound(pos, soundEvent, SoundCategory.HOSTILE, volume, pitch, 64.0)
        }
    }

    private fun createBurst(
        spikeGenerator: Spikes,
        positions: List<Vec3d>
    ) {
        val placedPositions = positions.flatMap { spikeGenerator.tryPlaceRift(entity.pos.add(it)) }.toList()

        eventScheduler.addEvent(TimedEvent({
            entity.sendSpikePacket(placedPositions)
        }, indicatorDelay, shouldCancel = shouldCancel))
    }

    companion object {
        const val indicatorDelay = 30
    }
}