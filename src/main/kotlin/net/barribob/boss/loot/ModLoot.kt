package net.barribob.boss.loot

import net.barribob.boss.Mod
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

class ModLoot {
    val gauntlet = RegistryKey.of(RegistryKeys.LOOT_TABLE, Mod.identifier("chests/gauntlet"))
    val obsidilith = RegistryKey.of(RegistryKeys.LOOT_TABLE, Mod.identifier("chests/obsidilith"))
}