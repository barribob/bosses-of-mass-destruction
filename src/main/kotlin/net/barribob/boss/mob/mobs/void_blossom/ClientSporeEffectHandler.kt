package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils

class ClientSporeEffectHandler(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler) : IStatusHandler {
    private val projectileParticles = ClientParticleBuilder(Particles.OBSIDILITH_BURST)
        .color(ModColors.GREEN)
        .colorVariation(0.4)
        .scale(0.5f)
        .brightness(Particles.FULL_BRIGHT)

    override fun handleClientStatus(status: Byte) {
        if(status == VoidBlossomAttacks.sporeAttack) {
            eventScheduler.addEvent(TimedEvent(::spawnParticles, 25, 15))
        }
    }

    private fun spawnParticles() {
        val pos = entity.eyePos.add(RandomUtils.randVec().multiply(3.0))
            .subtract(entity.rotationVecClient.multiply(2.0))
        val vel = VecUtils.yAxis.multiply(0.1)
        projectileParticles.build(pos, vel)
    }
}