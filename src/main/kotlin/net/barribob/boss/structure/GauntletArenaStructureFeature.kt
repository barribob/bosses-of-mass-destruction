package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructureGeneratorFactory
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.structure.StructurePiecesGenerator
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature

class GauntletArenaStructureFeature(codec: Codec<DefaultFeatureConfig>) : StructureFeature<DefaultFeatureConfig>(codec,
    StructureGeneratorFactory.simple(StructureGeneratorFactory.checkForBiomeOnTop(Heightmap.Type.WORLD_SURFACE_WG), ::addPieces)) {

    companion object {
        private val template: Identifier = Mod.identifier("gauntlet_arena")
        fun addPieces(collector : StructurePiecesCollector, context : StructurePiecesGenerator.Context<DefaultFeatureConfig>) {
            val blockPos = BlockPos(context.chunkPos().startX, 15, context.chunkPos().startZ)
            val rotation = BlockRotation.random(context.random)
            collector.addPiece(ModStructurePiece(context.structureManager, blockPos, template, rotation, ModStructures.gauntletArenaPiece))
        }
    }
}