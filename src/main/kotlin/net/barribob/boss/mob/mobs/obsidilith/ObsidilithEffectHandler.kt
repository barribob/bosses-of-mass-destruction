package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.entity.LivingEntity
import kotlin.math.sin

class ObsidilithEffectHandler(val entity: LivingEntity, val eventScheduler: EventScheduler) {
    private val burstParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ORANGE)
        .colorVariation(0.2)

    private val waveParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.RED)
        .colorVariation(0.2)

    private val spikeParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.COMET_BLUE)
        .colorVariation(0.2)

    private val anvilParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ENDER_PURPLE)
        .colorVariation(0.2)

    private val teleportFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
        .color(ModColors.ENDER_PURPLE)
        .brightness(Particles.FULL_BRIGHT)
        .age { RandomUtils.range(25, 30) }
        .colorVariation(0.2)

    private val deathParticleFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
        .color { age -> MathUtils.lerpVec(age, ModColors.ENDER_PURPLE, ModColors.WHITE) }
        .colorVariation(0.2)
        .brightness(Particles.FULL_BRIGHT)
        .age { RandomUtils.range(35, 40) }
        .scale { (sin(it.toDouble() * Math.PI * 0.5) + 1f).toFloat() * 0.1f }

    fun handleStatus(status: Byte) {
        when (status) {
            ObsidilithUtils.burstAttackStatus -> burstEffect()
            ObsidilithUtils.waveAttackStatus -> waveEffect()
            ObsidilithUtils.spikeAttackStatus -> spikeEffect()
            ObsidilithUtils.anvilAttackStatus -> anvilEffect()
            ObsidilithUtils.deathStatus -> deathEffect()
        }
    }

    private fun burstEffect() {
        val entityPos = entity.eyePos()
        for (i in 0..50) {
            val pos = entityPos.add(RandomUtils.randVec().normalize().multiply(3.0))
            val vel = MathUtils.unNormedDirection(pos, entityPos).crossProduct(VecUtils.yAxis).multiply(0.1)
            burstParticleFactory.build(pos, vel)
        }
    }

    private fun waveEffect() {
        val entityPos = entity.pos
        for (i in 0..50) {
            val randomYOffset = VecUtils.yAxis.multiply(entity.random.nextDouble())
            val randomYVel = VecUtils.yAxis.multiply(entity.random.nextDouble())
            val pos = entityPos.add(
                RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize().multiply(3.0)
            )
                .add(randomYOffset)

            waveParticleFactory.continuousVelocity {
                MathUtils.unNormedDirection(it.getPos(), entityPos)
                    .crossProduct(VecUtils.yAxis).negate()
                    .add(randomYVel).multiply(0.1)
            }.build(pos)
        }
    }

    private fun spikeEffect() {
        for (i in 0..50) {
            val entityPos = entity.pos
            val pos = entityPos.add(
                RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize().multiply(3.0)
            )
            spikeParticleFactory.continuousVelocity {
                MathUtils.unNormedDirection(it.getPos(), entityPos).crossProduct(VecUtils.yAxis).add(VecUtils.yAxis)
                    .multiply(0.1)
            }.build(pos)
        }
    }

    private fun anvilEffect() {
        for (i in 0..50) {
            val entityPos = entity.eyePos()
            val pos = entityPos.add(RandomUtils.randVec().normalize().multiply(3.0))
            val vel = MathUtils.unNormedDirection(pos, entityPos).crossProduct(VecUtils.yAxis).multiply(0.1)
            anvilParticleFactory.build(pos, vel)
        }

        eventScheduler.addEvent(TimedEvent({
            val particlePos = entity.pos.add(RandomUtils.randVec().multiply(3.0))
            val velocity = entity.velocity.multiply(0.7)
            teleportFactory.build(particlePos, velocity)
        }, 0, 80, { !entity.isAlive }))
    }

    private fun deathEffect() {
        val entityPos = entity.blockPos.asVec3d().add(0.5, 0.5, 0.5)
        for (i in 0..ObsidilithUtils.deathPillarHeight) {
            eventScheduler.addEvent(TimedEvent({
                MathUtils.circleCallback(3.0, 30, VecUtils.yAxis) {
                    deathParticleFactory.build(entityPos.add(it).add(VecUtils.yAxis.multiply(i.toDouble())))
                }
            }, i * ObsidilithUtils.ticksBetweenPillarLayer))
        }
    }
}