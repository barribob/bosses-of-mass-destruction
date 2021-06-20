package net.barribob.boss.structure.util

import net.minecraft.world.biome.SpawnSettings

fun interface IStructureSpawns {
    fun getMonsterSpawnList(): List<SpawnSettings.SpawnEntry>
}