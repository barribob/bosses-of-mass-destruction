package net.barribob.boss.config

import me.shedaniel.autoconfig.annotation.ConfigEntry

class LichConfig {
    val eternalNighttime = true

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f
    val health = 200.0
    val generateLichTower = true

    @ConfigEntry.Gui.CollapsibleObject
    val missile = Missile()

    @ConfigEntry.Gui.CollapsibleObject
    val comet = Comet()

    @ConfigEntry.Gui.CollapsibleObject
    val summonMechanic = SummonMechanic()

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
        val entitiesThatCountToSummonCounter: MutableList<String> = mutableListOf(
            "minecraft:zombie",
            "minecraft:skeleton",
            "minecraft:drowned",
            "minecraft:giant",
            "minecraft:husk",
            "minecraft:phantom",
            "minecraft:skeleton_horse",
            "minecraft:stray",
            "minecraft:wither",
            "minecraft:wither_skeleton",
            "minecraft:zoglin",
            "minecraft:zombie_horse",
            "minecraft:zombie_villager",
            "minecraft:zombified_piglin"
        ),

        @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
        val numEntitiesKilledToDropSoulStar: Int = 50
    )
}
