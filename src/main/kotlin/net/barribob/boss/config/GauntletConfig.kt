package net.barribob.boss.config

class GauntletConfig {

    val arenaGeneration = ArenaGeneration()

    data class ArenaGeneration(
        val generationHeight: Int = 10,
        val generationSpacing: Int = 32,
        val generationSeparation: Int = 24,
        val generationEnabled: Boolean = true
    )
}