package net.barribob.boss.config

class LichConfig {
    val eternalNighttime = true
    val experienceDrop = 1500
    val idleHealingPerTick = 0.2f
    val health = 200.0

    val missile = Missile()
    val comet = Comet()
    val summonMechanic = SummonMechanic()
    val arenaGeneration = ArenaGeneration()

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

    data class SummonMechanic(
        val isEnabled: Boolean = true,
        val entitiesThatCountToSummonCounter: List<String> = listOf(
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
        val numEntitiesKilledToDropSoulStar: Int = 50
    )

    data class ArenaGeneration(
        val generationEnabled: Boolean = true
    )
}
