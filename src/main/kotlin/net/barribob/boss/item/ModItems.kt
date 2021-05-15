package net.barribob.boss.item

import net.barribob.boss.Mod
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.ItemGroup
import net.minecraft.util.registry.Registry

class ModItems {
    val soulStar = SoulStarItem(FabricItemSettings().group(ItemGroup.MISC))

    fun init() {
        Registry.register(Registry.ITEM, Mod.identifier("soul_star"), soulStar)
    }
}