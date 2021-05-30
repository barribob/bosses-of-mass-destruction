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
}