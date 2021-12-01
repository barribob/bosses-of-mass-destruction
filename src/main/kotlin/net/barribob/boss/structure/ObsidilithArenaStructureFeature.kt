package net.barribob.boss.structure

import com.mojang.serialization.Codec
import kotlinx.coroutines.NonCancellable.children
import net.barribob.boss.Mod
import net.barribob.boss.config.ObsidilithConfig
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.*
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature


class ObsidilithArenaStructureFeature(
    codec: Codec<DefaultFeatureConfig>,
    private val obsidilithConfig: ObsidilithConfig
) :
    StructureFeature<DefaultFeatureConfig>(codec,
        StructureGeneratorFactory.simple(
            StructureGeneratorFactory.checkForBiomeOnTop(Heightmap.Type.WORLD_SURFACE_WG)
        ) { collector, context -> addPieces(collector, context, obsidilithConfig) }) {

    companion object {
        private val template: Identifier = Mod.identifier("obsidilith_arena")
        fun addPieces(collector : StructurePiecesCollector, context : StructurePiecesGenerator.Context<DefaultFeatureConfig>, obsidilithConfig: ObsidilithConfig) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = obsidilithConfig.arenaGeneration.generationHeight
            val blockPos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(context.random)
            collector.addPiece(ModStructurePiece(context.structureManager, blockPos, template, rotation, ModStructures.obsidilithArenaPiece))
        }

    }
}