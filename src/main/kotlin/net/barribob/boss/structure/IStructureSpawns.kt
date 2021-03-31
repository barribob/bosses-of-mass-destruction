package net.barribob.boss.structure

import net.minecraft.world.biome.SpawnSettings

fun interface IStructureSpawns {
    fun getMonsterSpawnList(): List<SpawnSettings.SpawnEntry>
}