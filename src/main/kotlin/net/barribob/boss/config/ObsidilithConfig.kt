package net.barribob.boss.config

class ObsidilithConfig {
    val health = 200.0
    val armor = 14.0
    val attack = 16.0
    val idleHealingPerTick = 0.5f
    val experienceDrop = 1000
    val spawnPillarOnDeath = true
    val anvilAttackExplosionStrength = 4.0f

    val arenaGeneration = ArenaGeneration()

    data class ArenaGeneration(
        val generationHeight: Int = 90,
        val generationSpacing: Int = 64,
        val generationSeparation: Int = 32,
        val generationEnabled: Boolean = true
    )
}