package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class ObsidilithConfig {
    val health = 200.0
    val armor = 14.0
    val attack = 16.0
    val idleHealingPerTick = 0.5f

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1000
    val spawnPillarOnDeath = true
    val anvilAttackExplosionStrength = 4.0f

    @ConfigEntry.Gui.CollapsibleObject
    val arenaGeneration = ArenaGeneration()

    data class ArenaGeneration(
        @ConfigEntry.BoundedDiscrete(min = 1, max = 150)
        val generationHeight: Int = 90,
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        val generationSpacing: Int = 64,

        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        val generationSeparation: Int = 32,
        val generationEnabled: Boolean = true
    )
}