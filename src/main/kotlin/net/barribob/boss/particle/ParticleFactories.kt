package net.barribob.boss.particle

import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils

object ParticleFactories {
    fun cometTrail() = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
        .brightness { Particles.FULL_BRIGHT }
        .scale { 0.5f + it * 0.3f }

    fun soulFlame() = ClientParticleBuilder(Particles.SOUL_FLAME)
        .brightness { Particles.FULL_BRIGHT }
}