package net.barribob.boss.block

import net.barribob.boss.Mod
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.util.registry.Registry

object ModBlocks {
    val obsidilithRune = ObsidilithRuneBlock(FabricBlockSettings.copy(Blocks.CRYING_OBSIDIAN))
    val obsidilithSummonBlock = ObsidilithSummonBlock(FabricBlockSettings.copy(Blocks.END_PORTAL_FRAME))
    private val gauntletBlackstone = GauntletBlackstoneBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN))
    private val sealedBlackstone = Block(FabricBlockSettings.copy(Blocks.BEDROCK))

    fun init(){
        Registry.register(Registry.BLOCK, Mod.identifier("obsidilith_rune"), obsidilithRune)
        Registry.register(Registry.ITEM, Mod.identifier("obsidilith_rune"), BlockItem(obsidilithRune, FabricItemSettings()))

        Registry.register(Registry.BLOCK, Mod.identifier("obsidilith_end_frame"), obsidilithSummonBlock)
        Registry.register(Registry.ITEM, Mod.identifier("obsidilith_end_frame"), BlockItem(obsidilithSummonBlock, FabricItemSettings()))

        Registry.register(Registry.BLOCK, Mod.identifier("gauntlet_blackstone"), gauntletBlackstone)
        Registry.register(Registry.ITEM, Mod.identifier("gauntlet_blackstone"), BlockItem(gauntletBlackstone, FabricItemSettings()))

        Registry.register(Registry.BLOCK, Mod.identifier("sealed_blackstone"), sealedBlackstone)
        Registry.register(Registry.ITEM, Mod.identifier("sealed_blackstone"), BlockItem(sealedBlackstone, FabricItemSettings()))
    }
}