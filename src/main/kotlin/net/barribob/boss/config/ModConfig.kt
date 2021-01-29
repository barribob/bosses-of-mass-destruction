package net.barribob.boss.config

import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import net.barribob.boss.Mod

@Config(name = Mod.MODID)
class ModConfig : ConfigData {
    val lichConfig = LichConfig()
    val obsidilithConfig = ObsidilithConfig()
}