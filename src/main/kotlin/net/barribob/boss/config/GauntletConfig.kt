package net.barribob.boss.config

class GauntletConfig {
    val health = 250.0
    val armor = 8.0
    val attack = 16.0
    val idleHealingPerTick = 0.5f
    val experienceDrop = 1000
    val spawnAncientDebrisOnDeath = true
    val energizedPunchExplosionSize = 4.5
    val normalPunchExplosionMultiplier = 1.5

    val arenaGeneration = ArenaGeneration()

    data class ArenaGeneration(
        val generationSpacing: Int = 32,
        val generationSeparation: Int = 24,
        val generationEnabled: Boolean = true
    )
}