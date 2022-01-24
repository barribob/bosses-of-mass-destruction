package net.barribob.boss.item

import net.barribob.boss.Mod
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.*
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.registry.Registry

class ModItems {
    val itemGroup: ItemGroup = FabricItemGroupBuilder.build(Mod.identifier("items")) { ItemStack(soulStar) }
    val soulStar = SoulStarItem(FabricItemSettings().group(itemGroup))
    private val ancientAnima = Item(FabricItemSettings().rarity(Rarity.RARE).group(itemGroup))
    private val blazingEye = Item(FabricItemSettings().rarity(Rarity.RARE).group(itemGroup).fireproof())
    private val obsidianHeart = Item(FabricItemSettings().rarity(Rarity.RARE).group(itemGroup).fireproof())
    private val earthdiveSpear = EarthdiveSpear(FabricItemSettings().group(itemGroup).fireproof().maxDamage(250))
    private val voidThorn = Item(FabricItemSettings().group(itemGroup).rarity(Rarity.RARE).fireproof())
    private val crystalFruitFoodComponent = FoodComponent.Builder().hunger(4).saturationModifier(1.2f)
        .statusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 300, 1), 1.0f)
        .statusEffect(StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1), 1.0f)
        .statusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 600, 0), 1.0f)
        .alwaysEdible().build()
    private val crystalFruit = CrystalFruitItem(FabricItemSettings().group(itemGroup).rarity(Rarity.RARE).fireproof().food(crystalFruitFoodComponent))
    val chargedEnderPearl = ChargedEnderPearlItem(FabricItemSettings().group(itemGroup).rarity(Rarity.RARE).fireproof())

    fun init() {
        Registry.register(Registry.ITEM, Mod.identifier("soul_star"), soulStar)
        Registry.register(Registry.ITEM, Mod.identifier("ancient_anima"), ancientAnima)
        Registry.register(Registry.ITEM, Mod.identifier("blazing_eye"), blazingEye)
        Registry.register(Registry.ITEM, Mod.identifier("obsidian_heart"), obsidianHeart)
        Registry.register(Registry.ITEM, Mod.identifier("earthdive_spear"), earthdiveSpear)
        Registry.register(Registry.ITEM, Mod.identifier("void_thorn"), voidThorn)
        Registry.register(Registry.ITEM, Mod.identifier("crystal_fruit"), crystalFruit)
        Registry.register(Registry.ITEM, Mod.identifier("charged_ender_pearl"), chargedEnderPearl)
    }

    @Environment(EnvType.CLIENT)
    fun clientInit() {
        FabricModelPredicateProviderRegistry.register(
            earthdiveSpear,
            Identifier("throwing")
        ) { itemStack, _, livingEntity, _ ->
            if (livingEntity == null) {
                return@register 0.0f
            }
            if (livingEntity.isUsingItem && livingEntity.activeItem === itemStack) 1.0f else 0.0f
        }
    }
}