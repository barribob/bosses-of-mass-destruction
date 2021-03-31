package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import kotlin.random.Random

class GauntletBlindnessIndicatorParticles(val entity: GauntletEntity, val eventScheduler: EventScheduler): IStatusHandler {
    private val particleBuilder = ClientParticleBuilder(Particles.EYE)
        .brightness(Particles.FULL_BRIGHT)
        .color{ MathUtils.lerpVec(it, ModColors.WHITE, ModColors.LASER_RED) }
    private val gauntletParticleBuilder = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color(ModColors.DARK_RED)
        .brightness(Particles.FULL_BRIGHT)
        .scale(0.25f)
        .age(20)

    fun handlePlayerEffects(players: List<PlayerEntity>) {
        for (player in players) {
            spawnRotatingParticles(player, particleBuilder)
        }
    }

    private fun spawnRotatingParticles(player: Entity, particleBuilder: ClientParticleBuilder) {
        for (i in 0..20) {
            val startingRotation = Random.nextInt(360)
            particleBuilder
                .continuousPosition {
                    calculatePosition(player, i, it.getAge(), startingRotation)
                }
                .build(calculatePosition(player, i, 0, startingRotation))
        }
    }

    private fun calculatePosition(
        player: Entity,
        i: Int,
        age: Int,
        startingRotation: Int
    ) = player.pos.add(VecUtils.yAxis.multiply(i * 0.1)).add(
        VecUtils.xAxis.rotateY(
            Math.toRadians((age * 2 + startingRotation).toDouble()).toFloat()
        )
    )

    override fun handleClientStatus(status: Byte) {
        if(status == GauntletAttacks.blindnessAttack) {
            eventScheduler.addEvent(TimedEvent({
                for(i in 0..3) {
                    val particlePos = entity.eyePos().add(RandomUtils.randVec().normalize().multiply(2.0))
                    val particleVel = RandomUtils.randVec().multiply(0.05).add(VecUtils.yAxis.multiply(0.05))
                    gauntletParticleBuilder.build(particlePos, particleVel)
                }
            }, 0, 10))
        }
    }
}