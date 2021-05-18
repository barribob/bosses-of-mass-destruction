package net.barribob.boss.block

import net.barribob.boss.Mod
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ModBlocks {
    val obsidilithRune = ObsidilithRuneBlock(FabricBlockSettings.copy(Blocks.CRYING_OBSIDIAN))
    val obsidilithSummonBlock = ObsidilithSummonBlock(FabricBlockSettings.copy(Blocks.END_PORTAL_FRAME))
    private val gauntletBlackstone = GauntletBlackstoneBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN))
    private val sealedBlackstone = Block(FabricBlockSettings.copy(Blocks.BEDROCK))
    val chiseledStoneAltar = ChiseledStoneAltarBlock(
        FabricBlockSettings.copy(Blocks.BEDROCK)
            .luminance { if (it.get(Properties.LIT)) 11 else 0 })

    private val entityTypes = mutableMapOf<Block, BlockEntityType<*>>()
    private val mobWardBlockEntityFactory: () -> BlockEntity = { ChunkCacheBlockEntity(mobWard, entityTypes[mobWard]) }
    val mobWard = MobWardBlock(mobWardBlockEntityFactory, FabricBlockSettings.copy(Blocks.OBSIDIAN))

    fun init() {
        registerBlockAndItem(Mod.identifier("obsidilith_rune"), obsidilithRune)
        registerBlockAndItem(Mod.identifier("obsidilith_end_frame"), obsidilithSummonBlock)
        registerBlockAndItem(Mod.identifier("gauntlet_blackstone"), gauntletBlackstone)
        registerBlockAndItem(Mod.identifier("sealed_blackstone"), sealedBlackstone)
        registerBlockAndItem(Mod.identifier("chiseled_stone_altar"), chiseledStoneAltar)

        val mobWardId = Mod.identifier("mob_ward")
        registerBlockAndItem(mobWardId, mobWard)

        entityTypes[mobWard] = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            mobWardId,
            BlockEntityType.Builder.create(mobWardBlockEntityFactory, mobWard).build(null)
        )
    }

    private fun registerBlockAndItem(identifier: Identifier, block: Block){
        Registry.register(Registry.BLOCK, identifier, block)
        Registry.register(Registry.ITEM, identifier, BlockItem(block, FabricItemSettings()))
    }
}