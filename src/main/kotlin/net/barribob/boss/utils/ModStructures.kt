package net.barribob.boss.utils

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.config.ModConfig
import net.barribob.boss.structure.GauntletArenaStructureFeature
import net.barribob.boss.structure.LichTowerStructureFeature
import net.barribob.boss.structure.ModStructurePiece
import net.barribob.boss.structure.ObsidilithArenaStructureFeature
import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomArenaStructureFeature
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomCavernPieceGenerator
import net.minecraft.entity.SpawnGroup
import net.minecraft.structure.StructurePieceType
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.collection.Pool
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.world.StructureSpawns
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.StructureTerrainAdaptation
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement
import net.minecraft.world.gen.chunk.placement.SpreadType


object ModStructures {
    val obsidilithStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("obsidilith_arena_piece"), StructureFactories.obsidilithArena)
    val obsidilithStructureRegistry = StructureRegister(Mod.identifier("obsidilith_arena"), net.minecraft.world.gen.structure.StructureType.createCodec { c ->
        ObsidilithArenaStructureFeature(
            c,
            obsidilithConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config.obsidilithConfig
        )
    })

    val gauntletStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("gauntlet_arena_piece"), StructureFactories.gauntletArena)
    val gauntletStructureRegistry = StructureRegister(Mod.identifier("gauntlet_arena"), net.minecraft.world.gen.structure.StructureType.createCodec(::GauntletArenaStructureFeature))

    val soulStarStructureKey: TagKey<net.minecraft.world.gen.structure.StructureType> = TagKey.of(Registry.STRUCTURE_KEY, Mod.identifier("soul_star_target"))
    val lichStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("lich_tower_piece"), StructureFactories.lichTower)
    val lichStructureRegistry = StructureRegister(Mod.identifier("lich_tower"), net.minecraft.world.gen.structure.StructureType.createCodec(::LichTowerStructureFeature))

    val voidLilyStructureKey: TagKey<net.minecraft.world.gen.structure.StructureType> = TagKey.of(Registry.STRUCTURE_KEY, Mod.identifier("void_lily_target"))
    val voidBlossomCavernPiece: StructurePieceType = createStructurePiece(Mod.identifier("void_blossom_piece"), StructureFactories.voidBlossom)
    val voidBlossomStructureRegistry = StructureRegister(Mod.identifier("void_blossom"), net.minecraft.world.gen.structure.StructureType.createCodec(::VoidBlossomArenaStructureFeature))

    fun init() {
        val modConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config
        val emptySpawns = mapOf(Pair(SpawnGroup.MONSTER, StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, Pool.empty())))
        val obsidilithGenConfig = modConfig.obsidilithConfig.arenaGeneration
        val gauntletGenConfig = modConfig.gauntletConfig.arenaGeneration
        val voidBlossomGenConfig = modConfig.voidBlossomConfig.arenaGeneration
        val lichConfig = modConfig.lichConfig.towerGeneration
        val noneHasStructure = "has_structure/none"

        val obsidilithStructureBiomeTagName = if (obsidilithGenConfig.generationEnabled) "has_structure/obsidilith_arena" else noneHasStructure
        val obsidilithStructureBiomeTag = getBiomeTag(obsidilithStructureBiomeTagName)
        val obsidilithStructureConfig = net.minecraft.world.gen.structure.StructureType.Config(BuiltinRegistries.BIOME.getOrCreateEntryList(obsidilithStructureBiomeTag), emptySpawns, GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)
        val obsidilithStructure = ObsidilithArenaStructureFeature(obsidilithStructureConfig, modConfig.obsidilithConfig)
        val obsidilithStructurePlacement = RandomSpreadStructurePlacement(obsidilithGenConfig.generationSpacing, obsidilithGenConfig.generationSeparation, SpreadType.LINEAR, 499672)
        obsidilithStructureRegistry.register(obsidilithStructure, obsidilithStructurePlacement)

        val voidBlossomStructureBiomeTagName = if(voidBlossomGenConfig.generationEnabled) "has_structure/void_blossom" else noneHasStructure
        val voidBlossomStructurePlacement = RandomSpreadStructurePlacement(voidBlossomGenConfig.generationSpacing, voidBlossomGenConfig.generationSeparation, SpreadType.LINEAR, 574839)
        val voidBlossomStructureBiomeTag: TagKey<Biome> = getBiomeTag(voidBlossomStructureBiomeTagName)
        val voidBlossomStructureConfig = net.minecraft.world.gen.structure.StructureType.Config(BuiltinRegistries.BIOME.getOrCreateEntryList(voidBlossomStructureBiomeTag), emptySpawns, GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)
        val voidBlossomStructure = VoidBlossomArenaStructureFeature(voidBlossomStructureConfig)
        voidBlossomStructureRegistry.register(voidBlossomStructure, voidBlossomStructurePlacement)

        val gauntletStructureBiomeTagName = if(gauntletGenConfig.generationEnabled) "has_structure/gauntlet_arena" else noneHasStructure
        val gauntletStructurePlacement = RandomSpreadStructurePlacement(gauntletGenConfig.generationSpacing, gauntletGenConfig.generationSeparation, SpreadType.LINEAR, 499672)
        val gauntletStructureBiomeTag = getBiomeTag(gauntletStructureBiomeTagName)
        val config = net.minecraft.world.gen.structure.StructureType.Config(BuiltinRegistries.BIOME.getOrCreateEntryList(gauntletStructureBiomeTag), emptySpawns, GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)
        val gauntletArenaStructure = GauntletArenaStructureFeature(config)
        gauntletStructureRegistry.register(gauntletArenaStructure, gauntletStructurePlacement)

        val lichStructureBiomeTagName = if(lichConfig.generateLichTower) "has_structure/lich_tower" else noneHasStructure
        val lichStructurePlacement = RandomSpreadStructurePlacement(lichConfig.lichTowerGenerationSpacing, lichConfig.lichTowerGenerationSeparation, SpreadType.LINEAR, 1230784)
        val lichStructureBiomeTag = getBiomeTag(lichStructureBiomeTagName)
        val lichStructureConfig = net.minecraft.world.gen.structure.StructureType.Config(BuiltinRegistries.BIOME.getOrCreateEntryList(lichStructureBiomeTag), emptySpawns, GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)
        val lichStructure = LichTowerStructureFeature(lichStructureConfig)
        lichStructureRegistry.register(lichStructure, lichStructurePlacement)
    }

    private fun getBiomeTag(identifier: String) = TagKey.of(Registry.BIOME_KEY, Mod.identifier(identifier))
    private fun createStructurePiece(identifier: Identifier, structurePieceType: StructurePieceType) = Registry.register(Registry.STRUCTURE_PIECE, identifier, structurePieceType)

    private object StructureFactories {
        val obsidilithArena = StructurePieceType { m, t -> ModStructurePiece(m.structureTemplateManager, t, obsidilithStructurePiece) }
        val gauntletArena = StructurePieceType { m, t -> ModStructurePiece(m.structureTemplateManager, t, gauntletStructurePiece) }
        val lichTower = StructurePieceType { m, t -> ModStructurePiece(m.structureTemplateManager, t, lichStructurePiece) }
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(voidBlossomCavernPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}