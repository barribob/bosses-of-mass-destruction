package net.barribob.boss.block

import net.barribob.boss.Mod
import net.barribob.boss.animation.IAnimationTimer
import net.barribob.boss.block.structure_repair.GauntletStructureRepair
import net.barribob.boss.block.structure_repair.LichStructureRepair
import net.barribob.boss.block.structure_repair.ObsidilithStructureRepair
import net.barribob.boss.block.structure_repair.VoidBlossomStructureRepair
import net.barribob.boss.mob.GeoModel
import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.ModBlockEntityRenderer
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.item.BlockItem
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ModBlocks {
    val obsidilithRune = ObsidilithRuneBlock(FabricBlockSettings.of(Material.STONE, MapColor.BLACK).requiresTool().breakByTool(FabricToolTags.PICKAXES, 3).strength(50.0F, 1200.0F))
    val voidBlossom = VoidBlossomBlock(FabricBlockSettings.of(Material.PLANT, MapColor.PURPLE).breakInstantly().noCollision().luminance { 11 }.sounds(BlockSoundGroup.SPORE_BLOSSOM))
    val vineWall = VineWallBlock(FabricBlockSettings.of(Material.PLANT, MapColor.DARK_GREEN).sounds(BlockSoundGroup.WOOD).strength(2.0f, 6.0f))
    val obsidilithSummonBlock = ObsidilithSummonBlock(FabricBlockSettings.copy(Blocks.END_PORTAL_FRAME))
    val gauntletBlackstone = GauntletBlackstoneBlock(FabricBlockSettings.of(Material.STONE, MapColor.BLACK).requiresTool().breakByTool(FabricToolTags.PICKAXES, 3).strength(50.0F, 1200.0F))
    val sealedBlackstone = Block(FabricBlockSettings.copy(Blocks.BEDROCK))
    val chiseledStoneAltar = ChiseledStoneAltarBlock(
        FabricBlockSettings.copy(Blocks.BEDROCK)
            .luminance { if (it.get(Properties.LIT)) 11 else 0 })

    var mobWardEntityType: BlockEntityType<ChunkCacheBlockEntity>? = null
    private val mobWardBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        ChunkCacheBlockEntity(mobWard, mobWardEntityType, pos, state)
    }
    val mobWard: MobWardBlock = MobWardBlock(mobWardBlockEntityFactory, FabricBlockSettings.of(Material.STONE, MapColor.BLACK).requiresTool().breakByTool(FabricToolTags.PICKAXES, 3)
        .nonOpaque()
        .luminance { 15 }
        .strength(10.0F, 1200.0F))

    var monolithEntityType: BlockEntityType<ChunkCacheBlockEntity>? = null
    private val monolithBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        ChunkCacheBlockEntity(monolithBlock, monolithEntityType, pos, state)
    }
    val monolithBlock: MonolithBlock = MonolithBlock(monolithBlockEntityFactory, FabricBlockSettings.of(Material.METAL, MapColor.BLACK).requiresTool().breakByTool(FabricToolTags.PICKAXES, 3)
        .nonOpaque()
        .luminance { 4 }
        .strength(10.0F, 1200.0F))

    var levitationBlockEntityType: BlockEntityType<LevitationBlockEntity>? = null
    private val levitationBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        LevitationBlockEntity(levitationBlock, levitationBlockEntityType, pos, state)
    }
    val levitationBlock: LevitationBlock = LevitationBlock(levitationBlockEntityFactory, FabricBlockSettings.of(Material.STONE, MapColor.BLUE).requiresTool().breakByTool(FabricToolTags.PICKAXES, 3)
        .nonOpaque()
        .luminance { 4 }
        .strength(10.0F, 1200.0F))

    var voidBlossomSummonBlockEntityType: BlockEntityType<VoidBlossomSummonBlockEntity>? = null
    private val voidBlossomSummonBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        VoidBlossomSummonBlockEntity(voidBlossomSummonBlockEntityType, pos, state)
    }
    val voidBlossomSummonBlock = VoidBlossomSummonBlock(voidBlossomSummonBlockEntityFactory, FabricBlockSettings.copy(Blocks.BEDROCK))

    var voidLilyBlockEntityType: BlockEntityType<VoidLilyBlockEntity>? = null
    private val voidLilyBlockEntityFactory = FabricBlockEntityTypeBuilder.Factory { pos, state ->
        VoidLilyBlockEntity(voidLilyBlockEntityType, pos, state)
    }
    private val voidLilyBlock = VoidLilyBlock(voidLilyBlockEntityFactory,
        FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly()
            .sounds(BlockSoundGroup.GRASS).luminance { 8 })

    fun init() {
        registerBlockAndItem(Mod.identifier("obsidilith_rune"), obsidilithRune)
        registerBlockAndItem(Mod.identifier("obsidilith_end_frame"), obsidilithSummonBlock)
        registerBlockAndItem(Mod.identifier("gauntlet_blackstone"), gauntletBlackstone)
        registerBlockAndItem(Mod.identifier("sealed_blackstone"), sealedBlackstone)
        registerBlockAndItem(Mod.identifier("chiseled_stone_altar"), chiseledStoneAltar)
        registerBlockAndItem(Mod.identifier("void_blossom"), voidBlossom)
        registerBlockAndItem(Mod.identifier("vine_wall"), vineWall)

        val mobWardId = Mod.identifier("mob_ward")
        val monolithBlockId = Mod.identifier("monolith_block")
        val levitationBlockId = Mod.identifier("levitation_block")
        val voidBlossomBlockId = Mod.identifier("void_blossom_block")
        val voidLilyBlockId = Mod.identifier("void_lily")

        registerBlockAndItem(mobWardId, mobWard, FabricItemSettings().group(Mod.items.itemGroup).fireproof())
        registerBlockAndItem(monolithBlockId, monolithBlock, FabricItemSettings().group(Mod.items.itemGroup).fireproof())
        registerBlockAndItem(levitationBlockId, levitationBlock, FabricItemSettings().group(Mod.items.itemGroup).fireproof())
        registerBlockAndItem(voidBlossomBlockId, voidBlossomSummonBlock, FabricItemSettings().fireproof())
        registerBlockAndItem(voidLilyBlockId, voidLilyBlock, FabricItemSettings().group(Mod.items.itemGroup))

        mobWardEntityType = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            mobWardId,
            FabricBlockEntityTypeBuilder.create(mobWardBlockEntityFactory, mobWard).build(null)
        )

        monolithEntityType = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            monolithBlockId,
            FabricBlockEntityTypeBuilder.create(monolithBlockEntityFactory, monolithBlock).build(null)
        )

        levitationBlockEntityType = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            levitationBlockId,
            FabricBlockEntityTypeBuilder.create(levitationBlockEntityFactory, levitationBlock).build(null)
        )

        voidBlossomSummonBlockEntityType = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            voidBlossomBlockId,
            FabricBlockEntityTypeBuilder.create(voidBlossomSummonBlockEntityFactory, voidBlossomSummonBlock).build(null)
        )

        voidLilyBlockEntityType = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            voidLilyBlockId,
            FabricBlockEntityTypeBuilder.create(voidLilyBlockEntityFactory, voidLilyBlock).build(null)
        )
    }

    fun clientInit(animationTimer: IAnimationTimer) {
        BlockRenderLayerMap.INSTANCE.putBlock(mobWard, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(voidBlossom, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(vineWall, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(voidLilyBlock, RenderLayer.getCutout())

        BlockEntityRendererRegistry.INSTANCE.register(levitationBlockEntityType, BlockEntityRendererFactory {
            ModBlockEntityRenderer(
                GeoModel<LevitationBlockEntity>(
                    { Mod.identifier("geo/levitation_block.geo.json") },
                    { Mod.identifier("textures/block/levitation_block.png") },
                    Mod.identifier("animations/levitation_block.animation.json"),
                    animationTimer
                )
            ) { _, _ -> IBoneLight.fullbright }
        })
    }

    private fun registerBlockAndItem(identifier: Identifier, block: Block, fabricItemSettings: FabricItemSettings = FabricItemSettings()) {
        Registry.register(Registry.BLOCK, identifier, block)
        Registry.register(Registry.ITEM, identifier, BlockItem(block, fabricItemSettings))
    }
}