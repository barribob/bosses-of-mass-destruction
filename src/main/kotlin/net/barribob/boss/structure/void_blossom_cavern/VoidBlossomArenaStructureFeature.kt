package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.serialization.Codec
import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructureGeneratorFactory
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.structure.StructurePiecesGenerator
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature

class VoidBlossomArenaStructureFeature(codec: Codec<DefaultFeatureConfig>) :
    StructureFeature<DefaultFeatureConfig>(codec,
        StructureGeneratorFactory.simple(
            StructureGeneratorFactory.checkForBiomeOnTop(Heightmap.Type.WORLD_SURFACE_WG),
            ::addPieces
        )) {

    companion object {
        fun addPieces(collector : StructurePiecesCollector, context : StructurePiecesGenerator.Context<DefaultFeatureConfig>) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = 35 + context.chunkGenerator.minimumY
            collector.addPiece(
                CodeStructurePiece(
                    ModStructures.voidBlossomPiece,
                    BlockBox(BlockPos(x, y, z)).expand(32),
                    VoidBlossomCavernPieceGenerator()
                )
            )
        }
    }
}