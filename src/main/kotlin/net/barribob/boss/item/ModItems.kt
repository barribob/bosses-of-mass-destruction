package net.barribob.boss.item

import net.barribob.boss.Mod
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class ModItems {
    val itemGroup: ItemGroup = FabricItemGroupBuilder.build(Mod.identifier("items")) { ItemStack(soulStar) }
    val soulStar = SoulStarItem(FabricItemSettings().group(itemGroup))
    private val ancientAnima = Item(FabricItemSettings().group(itemGroup))
    private val blazingEye = Item(FabricItemSettings().group(itemGroup).fireproof())
    private val obsidianHeart = Item(FabricItemSettings().group(itemGroup).fireproof())
    private val staff = EarthdiveSpear(FabricItemSettings().group(itemGroup).fireproof().maxDamage(250))

    fun init() {
        Registry.register(Registry.ITEM, Mod.identifier("soul_star"), soulStar)
        Registry.register(Registry.ITEM, Mod.identifier("ancient_anima"), ancientAnima)
        Registry.register(Registry.ITEM, Mod.identifier("blazing_eye"), blazingEye)
        Registry.register(Registry.ITEM, Mod.identifier("obsidian_heart"), obsidianHeart)
        Registry.register(Registry.ITEM, Mod.identifier("earthdive_spear"), staff)
    }

    @Environment(EnvType.CLIENT)
    fun clientInit() {
        FabricModelPredicateProviderRegistry.register(
            staff,
            Identifier("throwing")
        ) { itemStack, _, livingEntity, _ ->
            if (livingEntity == null) {
                return@register 0.0f
            }
            if (livingEntity.isUsingItem && livingEntity.activeItem === itemStack) 1.0f else 0.0f
        }
    }
}