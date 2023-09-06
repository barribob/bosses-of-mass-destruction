package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class VoidBlossomConfig {
    val health = 350.0
    val armor = 4.0
    val attack = 12.0
    val idleHealingPerTick = 0.5f

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1000
}