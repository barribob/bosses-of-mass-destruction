package net.barribob.boss.config

import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.TransitiveObject
import net.barribob.boss.Mod

@Config(name = Mod.MODID)
class ModConfig : ConfigData {
    @ConfigEntry.Category("Lich")
    @TransitiveObject
    val lichConfig = LichConfig()

    @ConfigEntry.Category("Obsidilith")
    @TransitiveObject
    val obsidilithConfig = ObsidilithConfig()

    @ConfigEntry.Category("Gauntlet")
    @TransitiveObject
    val gauntletConfig = GauntletConfig()

    @ConfigEntry.Category("VoidBlossom")
    @TransitiveObject
    val voidBlossomConfig = VoidBlossomConfig()

    fun postInit() {
        val entitiesThatCountToSummonCounter = lichConfig.summonMechanic.entitiesThatCountToSummonCounter
        if (entitiesThatCountToSummonCounter == null) {
            val defaultEntities = mutableListOf(
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
            )
            lichConfig.summonMechanic.entitiesThatCountToSummonCounter = defaultEntities.toMutableList()
        }
        else {
            lichConfig.summonMechanic.entitiesThatCountToSummonCounter =
                entitiesThatCountToSummonCounter.toSet().toMutableList()
        }
    }
}