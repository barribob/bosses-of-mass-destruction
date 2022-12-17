package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class GauntletConfig {
    val health = 250.0
    val armor = 8.0
    val attack = 16.0
    val idleHealingPerTick = 0.5f

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1000
    val spawnAncientDebrisOnDeath = true
    val energizedPunchExplosionSize = 4.5
    val normalPunchExplosionMultiplier = 1.5
}