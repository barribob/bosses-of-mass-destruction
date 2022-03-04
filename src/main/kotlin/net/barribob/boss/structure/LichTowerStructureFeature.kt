package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructureGeneratorFactory
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.structure.StructurePiecesGenerator
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature

class LichTowerStructureFeature(codec: Codec<DefaultFeatureConfig>) :
    StructureFeature<DefaultFeatureConfig>(codec,
        StructureGeneratorFactory.simple(
            StructureGeneratorFactory.checkForBiomeOnTop(Heightmap.Type.WORLD_SURFACE_WG),
            ::addPieces
        )) {

    companion object {
        fun addPieces(collector : StructurePiecesCollector, context : StructurePiecesGenerator.Context<DefaultFeatureConfig>) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = context.chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG, context.world) - 7
            val rotation = BlockRotation.random(context.random)
            val blockPos = BlockPos(x, y, z).add(BlockPos(-15, 0, -15).rotate(rotation))
            collector.addPiece(
                ModStructurePiece(
                    context.structureManager,
                    blockPos,
                    Mod.identifier("lich_tower_1"),
                    rotation,
                    ModStructures.lichStructurePiece
                )
            )
            collector.addPiece(
                ModStructurePiece(
                    context.structureManager,
                    blockPos.up(59 - 11),
                    Mod.identifier("lich_tower_2"),
                    rotation,
                    ModStructures.lichStructurePiece
                )
            )
        }
    }
}