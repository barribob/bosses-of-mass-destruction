package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject

class ClientDeathEffectHandler(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler) : IStatusHandler {
    private val deathParticle = ClientParticleBuilder(Particles.FLUFF)
        .color(ModColors.DARK_GREY)
        .colorVariation(0.1)
        .age(20, 30)
        .scale(0.3f)

    override fun handleClientStatus(status: Byte) {
        if(status.toInt() == 3) {
            val delay = 30
            val fallDirection = entity.rotationVecClient.planeProject(VecUtils.yAxis).rotateY(180f)
            val originPos = entity.pos.add(VecUtils.yAxis.multiply(2.0))
            eventScheduler.addEvent(TimedEvent({
                val pos = originPos
                    .add(RandomUtils.randVec().multiply(5.0))
                    .add(fallDirection.multiply(RandomUtils.double(6.0) + 6.0))
                val vel = RandomUtils.randVec().add(VecUtils.yAxis).multiply(0.05)
                deathParticle.build(pos, vel)
            }, delay, LightBlockRemover.deathMaxAge.toInt() - delay))
        }
    }
}