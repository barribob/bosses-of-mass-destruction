package net.barribob.boss.utils

import net.barribob.boss.block.BrimstoneNectarItemEffects

enum class VecId(val effectHandler: () -> Vec3dReceiver) {
    BrimstoneParticleEffect({ BrimstoneNectarItemEffects() });

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value }
    }
}