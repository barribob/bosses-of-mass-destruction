package net.barribob.boss.config

class LichConfig {
    val cometExplosionStrength = 4.0f
    val eternalNightime = true
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f

    val missile = Missile("minecraft:slowness", 100, 2)
    val summon = Summon("minecraft:phantom")

    data class Missile(val statusEffectId: String, val statusEffectDuration: Int, val statusEffectPotency: Int)
    data class Summon(val mobId: String, val summonNbt: String = "{}")

    val defaultNbt = """{Health: 200, Attributes: [{Name: 'generic.max_health', Base: 200.0},{Name: 'generic.follow_range', Base: 64},{Name: 'generic.attack_damage', Base: 9.0},{Name: 'generic.flying_speed', Base: 6.0}]}"""
}