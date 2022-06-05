package net.barribob.boss.structure.void_blossom_cavern

import net.barribob.boss.structure.util.CodeStructurePiece
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.structure.StructureType
import java.util.*

class VoidBlossomArenaStructureFeature(codec: Config) : StructureType(codec) {

    companion object {
        fun addPieces(collector : StructurePiecesCollector, context : Context) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = 35 + context.chunkGenerator.minimumY
            collector.addPiece(
                CodeStructurePiece(
                    ModStructures.voidBlossomCavernPiece,
                    BlockBox(BlockPos(x, y, z)).expand(32),
                    VoidBlossomCavernPieceGenerator()
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
        return ModStructures.voidBlossomStructureRegistry.structureTypeKey
    }
}