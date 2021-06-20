package net.barribob.boss.utils

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.Mod
import net.barribob.boss.config.ModConfig
import net.barribob.boss.structure.GauntletArenaStructureFeature
import net.barribob.boss.structure.LichTowerStructureFeature
import net.barribob.boss.structure.ModStructurePiece
import net.barribob.boss.structure.ObsidilithArenaStructureFeature
import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.structure.util.IStructureSpawns
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomCavernPieceGenerator
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomArenaStructureFeature
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder
import net.minecraft.entity.SpawnGroup
import net.minecraft.structure.StructurePieceType
import net.minecraft.util.Identifier
import net.minecraft.util.collection.Pool
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
    private val modConfig = AutoConfig.getConfigHolder(ModConfig::class.java).config

    val obsidilithArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("obsidilith_arena_piece"),
        StructureFactories.obsidilithArena
    )
    private val obsidilithArenaStructure =
        ObsidilithArenaStructureFeature(DefaultFeatureConfig.CODEC, modConfig.obsidilithConfig)
    private val configuredObsidilithStructure = obsidilithArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    val gauntletArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("gauntlet_arena_piece"),
        StructureFactories.gauntletArena
    )
    private val gauntletArenaStructure = GauntletArenaStructureFeature(DefaultFeatureConfig.CODEC)
    private val configuredGauntletStructure = gauntletArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    val lichTowerPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("lich_tower_piece"),
        StructureFactories.lichTower
    )
    val lichTowerStructure = LichTowerStructureFeature(DefaultFeatureConfig.CODEC)
    private val configuredLichTowerStructure = lichTowerStructure.configure(DefaultFeatureConfig.DEFAULT)

    val voidBlossomPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("void_blossom_piece"),
        StructureFactories.voidBlossom
    )
    val voidBlossomStructurePiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("void_blossom_structure_piece"),
        StructureFactories.voidBlossomStructure
    )
    private val voidBlossomArenaStructure = VoidBlossomArenaStructureFeature(DefaultFeatureConfig.CODEC)
    private val configuredVoidBlossomArenaStructure = voidBlossomArenaStructure.configure(DefaultFeatureConfig.DEFAULT)

    private val emptyStructureSpawn = IStructureSpawns { listOf() }
    private val structureSpawnRegistry: Map<StructureFeature<*>, IStructureSpawns> = mapOf(
        Pair(obsidilithArenaStructure, emptyStructureSpawn),
        Pair(gauntletArenaStructure, emptyStructureSpawn),
        Pair(voidBlossomArenaStructure, emptyStructureSpawn)
    )

    fun init() {
        val obsidilithGenConfig = modConfig.obsidilithConfig.arenaGeneration
        val gauntletGenConfig = modConfig.gauntletConfig.arenaGeneration

        FabricStructureBuilder.create(Mod.identifier("obsidilith_arena"), obsidilithArenaStructure)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(obsidilithGenConfig.generationSpacing, obsidilithGenConfig.generationSeparation, 499672)
            .register()

        FabricStructureBuilder.create(Mod.identifier("gauntlet_arena"), gauntletArenaStructure)
            .step(GenerationStep.Feature.UNDERGROUND_DECORATION)
            .defaultConfig(gauntletGenConfig.generationSpacing, gauntletGenConfig.generationSeparation, 499672)
            .register()

        FabricStructureBuilder.create(Mod.identifier("lich_tower"), lichTowerStructure)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(24, 12, 1230784)
            .register()

        FabricStructureBuilder.create(Mod.identifier("void_blossom"), voidBlossomArenaStructure)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 24, 574839)
            .register()

        if (obsidilithGenConfig.generationEnabled) {
            BiomeModifications.addStructure({
                BiomeSelectors.foundInTheEnd().test(it) && it.biomeKey != BiomeKeys.THE_END
            }, register(Mod.identifier("configured_obsidilith_arena"), configuredObsidilithStructure))
        }

        if (gauntletGenConfig.generationEnabled) {
            BiomeModifications.addStructure(
                BiomeSelectors.foundInTheNether()::test,
                register(Mod.identifier("configured_gauntlet_arena"), configuredGauntletStructure)
            )
        }

        if (modConfig.lichConfig.generateLichTower) {
            BiomeModifications.addStructure(
                { it.biome.temperature <= 0.05 && BiomeSelectors.foundInOverworld().test(it) },
                register(Mod.identifier("configured_lich_tower"), configuredLichTowerStructure)
            )
        }

        BiomeModifications.addStructure(
            BiomeSelectors.foundInOverworld()::test,
            register(Mod.identifier("configured_void_blossom_arena"), configuredVoidBlossomArenaStructure)
        )
    }

    private fun register(
        identifier: Identifier,
        configuredStructure: ConfiguredStructureFeature<*, *>
    ): RegistryKey<ConfiguredStructureFeature<*, *>> {
        val key = RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, identifier)
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, key.value, configuredStructure)
        return key
    }

    fun onGetSpawnEntries(
        structureAccessor: StructureAccessor,
        spawnGroup: SpawnGroup,
        blockPos: BlockPos,
        cir: CallbackInfoReturnable<Pool<SpawnSettings.SpawnEntry>>
    ) {
        if (spawnGroup == SpawnGroup.MONSTER) {
            val spawnRegistryEntry = structureSpawnRegistry.entries.firstOrNull {
                structureAccessor.getStructureAt(blockPos, false, it.key).hasChildren()
            } ?: return
            cir.returnValue = Pool.of(spawnRegistryEntry.value.getMonsterSpawnList())
        }
    }

    private object StructureFactories {
        val obsidilithArena = StructurePieceType { m, t -> ModStructurePiece(m, t, obsidilithArenaPiece) }
        val gauntletArena = StructurePieceType { m, t -> ModStructurePiece(m, t, gauntletArenaPiece) }
        val lichTower = StructurePieceType { m, t -> ModStructurePiece(m, t, lichTowerPiece) }
        val voidBlossomStructure = StructurePieceType { m, t -> ModStructurePiece(m, t, voidBlossomStructurePiece) }
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(voidBlossomPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}