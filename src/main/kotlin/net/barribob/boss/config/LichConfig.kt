package net.barribob.boss.config

class LichConfig {
    val cometExplosionStrength = 4.0f
    val eternalNightime = true
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f

    val missile = Missile()
    val summon = Summon()

    val defaultNbt =
        """{Health: 200, Attributes: [{Name: 'generic.max_health', Base: 200.0},{Name: 'generic.follow_range', Base: 64},{Name: 'generic.attack_damage', Base: 9.0},{Name: 'generic.flying_speed', Base: 6.0}]}"""

    data class Missile(
        val statusEffectId: String = "minecraft:slowness",
        val statusEffectDuration: Int = 100,
        val statusEffectPotency: Int = 2
    )

    data class Summon(val mobId: String = "minecraft:phantom", val summonNbt: String = "{}")
}

