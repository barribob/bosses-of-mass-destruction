package net.barribob.boss.utils

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.config.ModConfig
import net.barribob.boss.structure.ModPiece
import net.barribob.boss.structure.ObsidilithArenaStructureFeature
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder
import net.minecraft.structure.StructurePieceType
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.DefaultFeatureConfig

object ModStructures {
    private val mobConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val obsidilithArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("piece"),
        StructurePieceType { m, t -> ModPiece(m, t) })
    val obsidilithArenaStructure =
        ObsidilithArenaStructureFeature(DefaultFeatureConfig.CODEC, mobConfig.obsidilithConfig)
    private val configuredArenaStructure = obsidilithArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    fun init() {
        val arenaGeneration = mobConfig.obsidilithConfig.arenaGeneration

        FabricStructureBuilder.create(Mod.identifier("obsidilith_arena"), obsidilithArenaStructure)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(arenaGeneration.generationSpacing, arenaGeneration.generationSeparation, 499672)
            .register()

        val pieceRegistryKey = RegistryKey.of(
            Registry.CONFIGURED_STRUCTURE_FEATURE_WORLDGEN,
            Mod.identifier("configured_obsidilith_arena")
        )

        BuiltinRegistries.add(
            BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
            pieceRegistryKey.value,
            configuredArenaStructure
        )

        if (arenaGeneration.generationEnabled) {
            BiomeModifications.addStructure({
                BiomeSelectors.foundInTheEnd().test(it) && it.biomeKey != BiomeKeys.THE_END
            }, pieceRegistryKey)
        }
    }
}