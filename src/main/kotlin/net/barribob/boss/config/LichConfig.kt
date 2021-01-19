package net.barribob.boss.config

class LichConfig {
    val eternalNighttime = true
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f
    val health = 200.0

    val missile = Missile()
    val comet = Comet()

    data class Missile(
        val statusEffectId: String = "minecraft:slowness",
        val statusEffectDuration: Int = 100,
        val statusEffectPotency: Int = 2,
        val damage: Double = 9.0
    )

    data class Comet(
        val explosionStrength: Float = 4.0f,
        val destroysBlocks: Boolean = true
    )
}
