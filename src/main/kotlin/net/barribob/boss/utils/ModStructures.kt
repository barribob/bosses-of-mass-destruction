package net.barribob.boss.utils

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.config.ModConfig
import net.barribob.boss.structure.GauntletArenaStructureFeature
import net.barribob.boss.structure.IStructureSpawns
import net.barribob.boss.structure.ModPiece
import net.barribob.boss.structure.ObsidilithArenaStructureFeature
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder
import net.minecraft.entity.SpawnGroup
import net.minecraft.structure.StructurePieceType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.SpawnSettings
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.feature.ConfiguredStructureFeature
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object ModStructures {
    private val mobConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val obsidilithArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("obsidilith_arena_piece"),
        StructureFactories.obsidilithArena
    )
    private val obsidilithArenaStructure =
        ObsidilithArenaStructureFeature(DefaultFeatureConfig.CODEC, mobConfig.obsidilithConfig)
    private val configuredObsidilithStructure = obsidilithArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    val gauntletArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("gauntlet_arena_piece"),
        StructureFactories.gauntletArena
    )
    private val gauntletArenaStructure =
        GauntletArenaStructureFeature(DefaultFeatureConfig.CODEC, mobConfig.gauntletConfig)
    private val configuredGauntletStructure = gauntletArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    private val emptyStructureSpawn = IStructureSpawns { listOf() }
    private val structureSpawnRegistry: Map<StructureFeature<*>, IStructureSpawns> = mapOf(
        Pair(obsidilithArenaStructure, emptyStructureSpawn),
        Pair(gauntletArenaStructure, emptyStructureSpawn)
    )

    fun init() {
        val arenaGeneration = mobConfig.obsidilithConfig.arenaGeneration
        val gauntletArenaGeneration = mobConfig.obsidilithConfig.arenaGeneration

        FabricStructureBuilder.create(Mod.identifier("obsidilith_arena"), obsidilithArenaStructure)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(arenaGeneration.generationSpacing, arenaGeneration.generationSeparation, 499672)
            .register()

        FabricStructureBuilder.create(Mod.identifier("gauntlet_arena"), gauntletArenaStructure)
            .step(GenerationStep.Feature.UNDERGROUND_DECORATION)
            .defaultConfig(
                gauntletArenaGeneration.generationSpacing,
                gauntletArenaGeneration.generationSeparation,
                499672
            )
            .register()


        if (arenaGeneration.generationEnabled) {
            BiomeModifications.addStructure({
                BiomeSelectors.foundInTheEnd().test(it) && it.biomeKey != BiomeKeys.THE_END
            }, register(Mod.identifier("configured_obsidilith_arena"), configuredObsidilithStructure))
        }

        if (gauntletArenaGeneration.generationEnabled) {
            BiomeModifications.addStructure(
                BiomeSelectors.foundInTheNether()::test,
                register(Mod.identifier("configured_gauntlet_arena"), configuredGauntletStructure)
            )
        }
    }

    private fun register(
        identifier: Identifier,
        configuredStructure: ConfiguredStructureFeature<*, *>
    ): RegistryKey<ConfiguredStructureFeature<*, *>> {
        val key = RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_WORLDGEN, identifier)
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, key.value, configuredStructure)
        return key
    }

    fun onGetSpawnEntries(
        structureAccessor: StructureAccessor,
        spawnGroup: SpawnGroup,
        blockPos: BlockPos,
        cir: CallbackInfoReturnable<List<SpawnSettings.SpawnEntry>>
    ) {
        if (spawnGroup == SpawnGroup.MONSTER) {
            val spawnRegistryEntry = structureSpawnRegistry.entries.firstOrNull {
                structureAccessor.getStructureAt(blockPos, false, it.key).hasChildren()
            } ?: return
            cir.returnValue = spawnRegistryEntry.value.getMonsterSpawnList()
        }
    }

    private object StructureFactories {
        val obsidilithArena = StructurePieceType { m, t -> ModPiece(m, t, obsidilithArenaPiece) }
        val gauntletArena = StructurePieceType { m, t -> ModPiece(m, t, gauntletArenaPiece) }
    }
}