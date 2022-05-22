package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class LichConfig {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f
    val health = 300.0

    @ConfigEntry.Gui.CollapsibleObject
    val missile = Missile()

    @ConfigEntry.Gui.CollapsibleObject
    val comet = Comet()

    @ConfigEntry.Gui.CollapsibleObject
    val summonMechanic = SummonMechanic()

    @ConfigEntry.Gui.CollapsibleObject
    val towerGeneration = Generation()

    data class Missile(
        val statusEffectId: String = "minecraft:slowness",

        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        val statusEffectDuration: Int = 100,

        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        val statusEffectPotency: Int = 2,
        val damage: Double = 9.0
    )

    data class Comet(
        val explosionStrength: Float = 4.0f,
        val destroysBlocks: Boolean = true
    )

    data class SummonMechanic(
        val isEnabled: Boolean = true,

        var entitiesThatCountToSummonCounter: MutableList<String>? = null,

        @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
        val numEntitiesKilledToDropSoulStar: Int = 50
    )

    data class Generation(
        val generateLichTower: Boolean = true,
        val lichTowerGenerationSpacing: Int = 100,
        val lichTowerGenerationSeparation: Int = 50
    )
}
