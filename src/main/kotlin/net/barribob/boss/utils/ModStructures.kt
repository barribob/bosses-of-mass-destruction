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
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomArenaStructureFeature
import net.barribob.boss.structure.void_blossom_cavern.VoidBlossomCavernPieceGenerator
import net.minecraft.class_6908
import net.minecraft.entity.SpawnGroup
import net.minecraft.structure.StructurePieceType
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.collection.Pool
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.SpawnSettings
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
    val obsidilithArenaStructureTagKey = TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, Mod.identifier("obsidilith_arena"))
    val obsidilithArenaStructure =
        ObsidilithArenaStructureFeature(DefaultFeatureConfig.CODEC, modConfig.obsidilithConfig)
    val configuredObsidilithStructure = obsidilithArenaStructure.configure(DefaultFeatureConfig.DEFAULT, class_6908.MINESHAFT_HAS_STRUCTURE)

    val gauntletArenaPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("gauntlet_arena_piece"),
        StructureFactories.gauntletArena
    )
    val gauntletArenaStructure = GauntletArenaStructureFeature(DefaultFeatureConfig.CODEC)
    val configuredGauntletStructure = gauntletArenaStructure.configure(DefaultFeatureConfig.DEFAULT, class_6908.MINESHAFT_HAS_STRUCTURE)

    val lichStructureTagKey = TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, Mod.identifier("lich_tower"))
    val lichTowerPiece: StructurePieceType = Registry.register(
        Registry.STRUCTURE_PIECE,
        Mod.identifier("lich_tower_piece"),
        StructureFactories.lichTower
    )
    val lichTowerStructure = LichTowerStructureFeature(DefaultFeatureConfig.CODEC)
    val configuredLichTowerStructure = lichTowerStructure.configure(DefaultFeatureConfig.DEFAULT, class_6908.MINESHAFT_HAS_STRUCTURE)

    val voidBlossomStructureArenaTagKey = TagKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, Mod.identifier("void_blossom"))
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
    val voidBlossomArenaStructure = VoidBlossomArenaStructureFeature(DefaultFeatureConfig.CODEC)
    val configuredVoidBlossomArenaStructure = voidBlossomArenaStructure.configure(DefaultFeatureConfig.DEFAULT, class_6908.MINESHAFT_HAS_STRUCTURE)

    private val emptyStructureSpawn = IStructureSpawns { listOf() }
    private val structureSpawnRegistry: Map<StructureFeature<*>, IStructureSpawns> = mapOf(
        Pair(obsidilithArenaStructure, emptyStructureSpawn),
        Pair(gauntletArenaStructure, emptyStructureSpawn),
        Pair(voidBlossomArenaStructure, emptyStructureSpawn)
    )

    fun init() {
        val obsidilithGenConfig = modConfig.obsidilithConfig.arenaGeneration
        val gauntletGenConfig = modConfig.gauntletConfig.arenaGeneration
        val voidBlossomGenConfig = modConfig.voidBlossomConfig.arenaGeneration
        val lichConfig = modConfig.lichConfig.towerGeneration

//        FabricStructureBuilder.create(Mod.identifier("obsidilith_arena"), obsidilithArenaStructure)
//            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
//            .defaultConfig(obsidilithGenConfig.generationSpacing, obsidilithGenConfig.generationSeparation, 499672)
//            .register()
//
//
//        FabricStructureBuilder.create(Mod.identifier("gauntlet_arena"), gauntletArenaStructure)
//            .step(GenerationStep.Feature.UNDERGROUND_DECORATION)
//            .defaultConfig(gauntletGenConfig.generationSpacing, gauntletGenConfig.generationSeparation, 499672)
//            .register()
//
//        FabricStructureBuilder.create(Mod.identifier("lich_tower"), lichTowerStructure)
//            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
//            .defaultConfig(lichConfig.lichTowerGenerationSpacing, lichConfig.lichTowerGenerationSeparation, 1230784)
//            .register()
//
//        FabricStructureBuilder.create(Mod.identifier("void_blossom"), voidBlossomArenaStructure)
//            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
//            .defaultConfig(voidBlossomGenConfig.generationSpacing, voidBlossomGenConfig.generationSeparation, 574839)
//            .register()

        register(Mod.identifier("configured_obsidilith_arena"), configuredObsidilithStructure)
        register(Mod.identifier("configured_gauntlet_arena"), configuredGauntletStructure)
        register(Mod.identifier("configured_lich_tower"), configuredLichTowerStructure)
        register(Mod.identifier("configured_void_blossom_arena"), configuredVoidBlossomArenaStructure)
//        if (obsidilithGenConfig.generationEnabled) {
//            BiomeModifications.({
//                BiomeSelectors.foundInTheEnd().test(it) && it.biomeKey != BiomeKeys.THE_END
//            }, register(Mod.identifier("configured_obsidilith_arena"), configuredObsidilithStructure))
//        }
//
//        if (gauntletGenConfig.generationEnabled) {
//            BiomeModifications.addStructure(
//                BiomeSelectors.foundInTheNether()::test,
//                register(Mod.identifier("configured_gauntlet_arena"), configuredGauntletStructure)
//            )
//        }
//
//        if (lichConfig.generateLichTower) {
//            BiomeModifications.addStructure(
//                { it.biome.temperature <= 0.05 && BiomeSelectors.foundInOverworld().test(it) },
//                register(Mod.identifier("configured_lich_tower"), configuredLichTowerStructure)
//            )
//        }
//
//        if (voidBlossomGenConfig.generationEnabled) {
//            BiomeModifications.addStructure(
//                BiomeSelectors.foundInOverworld()::test,
//                register(Mod.identifier("configured_void_blossom_arena"), configuredVoidBlossomArenaStructure)
//            )
//        }

//        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { _, world ->
//            val chunkGenerator = world.chunkManager.chunkGenerator
//            if (chunkGenerator is FlatChunkGenerator && world.registryKey == World.OVERWORLD) {
//                val map = chunkGenerator.structuresConfig.structures.filter {
//                    !listOf(
//                        obsidilithArenaStructure,
//                        lichTowerStructure,
//                        gauntletArenaStructure,
//                        voidBlossomArenaStructure
//                    ).contains(it.key)
//                }.toMap()
//                (chunkGenerator.structuresConfig as StructuresConfigAccessor).bossesOfMassDestruction_setStructures(map)
//            }
//        })
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
//        if (spawnGroup == SpawnGroup.MONSTER) {
//            val spawnRegistryEntry = structureSpawnRegistry.entries.firstOrNull {
//                structureAccessor.getStructureAt(blockPos, it.key).hasChildren()
//            } ?: return
//            cir.returnValue = Pool.of(spawnRegistryEntry.value.getMonsterSpawnList())
//        }
    }

    private object StructureFactories {
        val obsidilithArena = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, obsidilithArenaPiece) }
        val gauntletArena = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, gauntletArenaPiece) }
        val lichTower = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, lichTowerPiece) }
        val voidBlossomStructure = StructurePieceType { m, t -> ModStructurePiece(m.structureManager, t, voidBlossomStructurePiece) }
        val voidBlossom: StructurePieceType = StructurePieceType { _, t -> CodeStructurePiece(voidBlossomPiece, t, VoidBlossomCavernPieceGenerator()) }
    }
}