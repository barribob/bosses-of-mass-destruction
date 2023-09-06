package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.block.BrimstoneNectarItem
import net.barribob.boss.block.structure_repair.GauntletStructureRepair
import net.barribob.boss.block.structure_repair.LichStructureRepair
import net.barribob.boss.block.structure_repair.ObsidilithStructureRepair
import net.barribob.boss.block.structure_repair.VoidBlossomStructureRepair
import net.barribob.boss.utils.ModUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity

class ModItems {
    var itemGroup: RegistryKey<ItemGroup> = RegistryKey.of(RegistryKeys.ITEM_GROUP, Mod.identifier("items"))
    val soulStar = SoulStarItem(FabricItemSettings())
    private val ancientAnima = MaterialItem(FabricItemSettings().rarity(Rarity.RARE))
    private val blazingEye = MaterialItem(FabricItemSettings().rarity(Rarity.RARE).fireproof())
    private val obsidianHeart = MaterialItem(FabricItemSettings().rarity(Rarity.RARE).fireproof())
    private val earthdiveSpear = EarthdiveSpear(FabricItemSettings().fireproof().maxDamage(250))
    private val voidThorn = MaterialItem(FabricItemSettings().rarity(Rarity.RARE).fireproof())
    private val crystalFruitFoodComponent = FoodComponent.Builder().hunger(4).saturationModifier(1.2f)
        .statusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 300, 1), 1.0f)
        .statusEffect(StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1), 1.0f)
        .statusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 600, 0), 1.0f)
        .alwaysEdible().build()
    private val crystalFruit = CrystalFruitItem(FabricItemSettings().rarity(Rarity.RARE).fireproof().food(crystalFruitFoodComponent))
    val chargedEnderPearl = ChargedEnderPearlItem(FabricItemSettings().fireproof().maxCount(1))
    private val brimstoneNectar = BrimstoneNectarItem(
        FabricItemSettings().rarity(Rarity.RARE).fireproof(), listOf(
        VoidBlossomStructureRepair(), GauntletStructureRepair(), ObsidilithStructureRepair(), LichStructureRepair()
    ))

    fun init() {
        Registry.register(Registries.ITEM_GROUP, itemGroup, FabricItemGroup.builder()
            .icon { ItemStack(soulStar) }
            .displayName(Text.translatable("itemGroup.bosses_of_mass_destruction.items"))
            .build())
        
        registerItem(Mod.identifier("soul_star"), soulStar)
        registerItem(Mod.identifier("ancient_anima"), ancientAnima)
        registerItem(Mod.identifier("blazing_eye"), blazingEye)
        registerItem(Mod.identifier("obsidian_heart"), obsidianHeart)
        registerItem(Mod.identifier("earthdive_spear"), earthdiveSpear)
        registerItem(Mod.identifier("void_thorn"), voidThorn)
        registerItem(Mod.identifier("crystal_fruit"), crystalFruit)
        registerItem(Mod.identifier("charged_ender_pearl"), chargedEnderPearl)
        registerItem(Mod.identifier("brimstone_nectar"), brimstoneNectar)
    }
    
    private fun registerItem(identifier: Identifier, item: Item, addToItemGroup: Boolean = true){
        Registry.register(Registries.ITEM, identifier, item)
        if (addToItemGroup)
            ModUtils.addItemToGroup(item)
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