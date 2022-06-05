package net.barribob.boss.structure

import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.structure.StructureType
import java.util.*

class LichTowerStructureFeature(codec: Config) : StructureType(codec) {

    companion object {
        fun addPieces(collector : StructurePiecesCollector, context : Context) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = context.chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG, context.world, context.noiseConfig) - 7
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

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        return getStructurePosition(
            context, Heightmap.Type.WORLD_SURFACE_WG
        ) { collector: StructurePiecesCollector? -> addPieces(collector as StructurePiecesCollector, context) }
    }


    override fun getType(): net.minecraft.structure.StructureType<*> {
        return ModStructures.lichStructureRegistry.structureTypeKey
    }
}