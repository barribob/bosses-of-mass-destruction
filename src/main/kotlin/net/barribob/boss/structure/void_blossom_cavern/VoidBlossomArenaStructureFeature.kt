package net.barribob.boss.structure.void_blossom_cavern

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.structure.util.CodeStructurePiece
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType
import java.util.*

class VoidBlossomArenaStructureFeature(codec: Config) : Structure(codec) {

    companion object {
        fun addPieces(collector : StructurePiecesCollector, context : Context) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = 35 + context.chunkGenerator.minimumY
            collector.addPiece(
                CodeStructurePiece(
                    Mod.structures.voidBlossomCavernPiece,
                    BlockBox(BlockPos(x, y, z)).expand(32),
                    VoidBlossomCavernPieceGenerator()
                )
            )
        }
        
        val CODEC: Codec<VoidBlossomArenaStructureFeature> = createCodec(::VoidBlossomArenaStructureFeature)
    }

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        return getStructurePosition(
            context, Heightmap.Type.WORLD_SURFACE_WG
        ) { collector: StructurePiecesCollector? -> addPieces(collector as StructurePiecesCollector, context) }
    }

    override fun getType(): StructureType<*> {
        return Mod.structures.voidBlossomStructureType
    }
}