package net.barribob.boss.structure

import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType
import java.util.*

class GauntletArenaStructureFeature(codec: Config) : Structure(codec) {

    companion object {
        private val template: Identifier = Mod.identifier("gauntlet_arena")
        fun addPieces(collector: StructurePiecesCollector, context: Context) {
            val blockPos = BlockPos(context.chunkPos().startX, 15, context.chunkPos().startZ)
            val rotation = BlockRotation.random(context.random)
            collector.addPiece(ModStructurePiece(context.structureTemplateManager, blockPos, template, rotation, ModStructures.gauntletStructurePiece))
        }
    }

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        return getStructurePosition(
            context, Heightmap.Type.WORLD_SURFACE_WG
        ) { collector: StructurePiecesCollector? -> addPieces(collector as StructurePiecesCollector, context) }
    }

    override fun getType(): StructureType<*> {
        return ModStructures.gauntletStructureRegistry.structureTypeKey
    }
}