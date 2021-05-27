package net.barribob.boss.item

import net.barribob.boss.Mod
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.registry.Registry

class ModItems {
    val soulStar = SoulStarItem(FabricItemSettings().group(ItemGroup.MISC))
    private val ancientAnima = Item(FabricItemSettings().group(ItemGroup.MISC))
    val blazingEye = Item(FabricItemSettings().group(ItemGroup.MISC).fireproof())
    private val obsidianHeart = Item(FabricItemSettings().group(ItemGroup.MISC).fireproof())

    fun init() {
        Registry.register(Registry.ITEM, Mod.identifier("soul_star"), soulStar)
        Registry.register(Registry.ITEM, Mod.identifier("ancient_anima"), ancientAnima)
        Registry.register(Registry.ITEM, Mod.identifier("blazing_eye"), blazingEye)
        Registry.register(Registry.ITEM, Mod.identifier("obsidian_heart"), obsidianHeart)
    }
}