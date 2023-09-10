package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class GeneralConfig {
    @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
    val tableOfElevationRadius = 3
}