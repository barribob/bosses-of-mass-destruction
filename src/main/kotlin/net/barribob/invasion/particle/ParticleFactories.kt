package net.barribob.invasion.particle

import net.barribob.invasion.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils

object ParticleFactories {
    val COMET_TRAIL = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
        .brightness { Particles.FULL_BRIGHT }
        .scale { 0.5f + it * 0.3f }
}