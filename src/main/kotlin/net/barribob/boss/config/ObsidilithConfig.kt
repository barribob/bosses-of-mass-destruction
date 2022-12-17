package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class ObsidilithConfig {
    val health = 300.0
    val armor = 14.0
    val attack = 16.0
    val idleHealingPerTick = 0.5f

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1000
    val spawnPillarOnDeath = true
    val anvilAttackExplosionStrength = 4.0f
}