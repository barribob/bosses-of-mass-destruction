package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.LivingEntity

class ObsidilithEffectHandler(val entity: LivingEntity) {
    private val burstParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.ORANGE)
        .colorVariation(0.2)

    private val waveParticleFactory = ClientParticleBuilder(Particles.ENCHANT)
        .color(ModColors.RED)
        .colorVariation(0.2)

    fun handleStatus(status: Byte) {
        if(status == ObsidilithUtils.burstAttackStatus) {
            burstEffect()
        } else if(status == ObsidilithUtils.waveAttackStatus) {
            waveEffect()
        }
    }

    private fun burstEffect() {
        for (i in 0..50) {
            val pos = entity.eyePos().add(RandomUtils.randVec().normalize().multiply(3.0))
            burstParticleFactory.velocity {
                MathUtils.unNormedDirection(pos, entity.eyePos()).crossProduct(VecUtils.yAxis).multiply(0.1)
            }.build(pos)
        }
    }

    private fun waveEffect() {
        for (i in 0..50) {
            val pos = entity.eyePos().add(RandomUtils.randVec().normalize().multiply(3.0))
            waveParticleFactory.velocity {
                MathUtils.unNormedDirection(pos, entity.eyePos()).crossProduct(VecUtils.yAxis).multiply(0.1)
            }.build(pos)
        }
    }
}