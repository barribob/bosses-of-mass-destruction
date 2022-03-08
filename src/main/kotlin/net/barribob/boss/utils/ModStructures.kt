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
import net.minecraft.util.registry.Registry
import net.minecraft.world.StructureSpawns
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement
import net.minecraft.world.gen.chunk.placement.SpreadType
import net.minecraft.world.gen.feature.ConfiguredStructureFeature
import net.minecraft.world.gen.feature.DefaultFeatureConfig


object ModStructures {
    val obsidilithStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("obsidilith_arena_piece"), StructureFactories.obsidilithArena)
    val obsidilithStructureRegistry = StructureRegister(Mod.identifier("obsidilith_arena"))

    val gauntletStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("gauntlet_arena_piece"), StructureFactories.gauntletArena)
    val gauntletStructureRegistry = StructureRegister(Mod.identifier("gauntlet_arena"))

    val soulStarStructureKey: TagKey<ConfiguredStructureFeature<*, *>> = TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, Mod.identifier("soul_star_target"))
    val lichStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("lich_tower_piece"), StructureFactories.lichTower)
    val lichStructureRegistry = StructureRegister(Mod.identifier("lich_tower"))

    val voidLilyStructureKey: TagKey<ConfiguredStructureFeature<*, *>> = TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, Mod.identifier("void_lily_target"))
    val voidBlossomCavernPiece: StructurePieceType = createStructurePiece(Mod.identifier("void_blossom_piece"), StructureFactories.voidBlossom)
    val voidBlossomStructurePiece: StructurePieceType = createStructurePiece(Mod.identifier("void_blossom_structure_piece"), StructureFactories.voidBlossomStructure)
    val voidBlossomStructureRegistry = StructureRegister(Mod.identifier("void_blossom"))

    fun init() {
        val modConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config
        val emptySpawns = mapOf(Pair(SpawnGroup.MONSTER, StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, Pool.empty())))
        val obsidilithGenConfig = modConfig.obsidilithConfig.arenaGeneration
        val gauntletGenConfig = modConfig.gauntletConfig.arenaGeneration
        val voidBlossomGenConfig = modConfig.voidBlossomConfig.arenaGeneration
        val lichConfig = modConfig.lichConfig.towerGeneration

        val obsidilithStructureBiomeTag = getBiomeTag("has_structure/obsidilith_arena")
        val obsidilithStructure = ObsidilithArenaStructureFeature(DefaultFeatureConfig.CODEC, modConfig.obsidilithConfig)
        val obsidilithStructurePlacement = RandomSpreadStructurePlacement(obsidilithGenConfig.generationSpacing, obsidilithGenConfig.generationSeparation, SpreadType.LINEAR, 499672)
        val obsidilithConfiguredStructure = obsidilithStructure.configure(DefaultFeatureConfig.INSTANCE, obsidilithStructureBiomeTag, emptySpawns)
        obsidilithStructureRegistry.register(obsidilithStructure, obsidilithConfiguredStructure, obsidilithStructurePlacement)

        val voidBlossomStructurePlacement = RandomSpreadStructurePlacement(voidBlossomGenConfig.generationSpacing, voidBlossomGenConfig.generationSeparation, SpreadType.LINEAR, 574839)
        val voidBlossomStructure = VoidBlossomArenaStructureFeature(DefaultFeatureConfig.CODEC)
        val voidBlossomStructureBiomeTag: TagKey<Biome> = getBiomeTag("has_structure/void_blossom")
        val voidBlossomConfiguredStructure: ConfiguredStructureFeature<*, *> = voidBlossomStructure.configure(DefaultFeatureConfig.INSTANCE, voidBlossomStructureBiomeTag, emptySpawns)
        voidBlossomStructureRegistry.register(voidBlossomStructure, voidBlossomConfiguredStructure, voidBlossomStructurePlacement)

        val gauntletStructurePlacement = RandomSpreadStructurePlacement(gauntletGenConfig.generationSpacing, gauntletGenConfig.generationSeparation, SpreadType.LINEAR, 499672)
        val gauntletStructureBiomeTag = getBiomeTag("has_structure/gauntlet_arena")
        val gauntletArenaStructure = GauntletArenaStructureFeature(DefaultFeatureConfig.CODEC)
        val gauntletConfiguredStructure = gauntletArenaStructure.configure(DefaultFeatureConfig.INSTANCE, gauntletStructureBiomeTag, emptySpawns)
        gauntletStructureRegistry.register(gauntletArenaStructure, gauntletConfiguredStructure, gauntletStructurePlacement)

        val lichStructurePlacement = RandomSpreadStructurePlacement(lichConfig.lichTowerGenerationSpacing, lichConfig.lichTowerGenerationSeparation, SpreadType.LINEAR, 1230784)
        val lichStructureBiomeTag = getBiomeTag("has_structure/lich_tower")
        val lichStructure = LichTowerStructureFeature(DefaultFeatureConfig.CODEC)
        val lichConfiguredStructure = lichStructure.configure(DefaultFeatureConfig.INSTANCE, lichStructureBiomeTag)
        lichStructureRegistry.register(lichStructure, lichConfiguredStructure, lichStructurePlacement)
    }

    private fun getBiomeTag(identifier: String) = TagKey.of(Registry.BIOME_KEY, Mod.identifier(identifier))
    private fun createStructurePiece(identifier: Identifier, structurePieceType: StructurePieceType) = Registry.register(Registry.STRUCTURE_PIECE, identifier, structurePieceType)

    private object StructureFactories {
        val obsidilithArena = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, obsidilithStructurePiece) }
        val gauntletArena = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, gauntletStructurePiece) }
        val lichTower = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, lichStructurePiece) }
        val voidBlossomStructure = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, voidBlossomStructurePiece) }
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(voidBlossomCavernPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}