package net.barribob.boss.structure

import net.barribob.boss.Mod
import net.barribob.boss.config.ObsidilithConfig
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructurePiecesCollector
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.structure.StructureType
import java.util.*


class ObsidilithArenaStructureFeature(
    codec: Config,
    private val obsidilithConfig: ObsidilithConfig
) :
    StructureType(codec) {

    companion object {
        private val template: Identifier = Mod.identifier("obsidilith_arena")
        fun addPieces(collector: StructurePiecesCollector, context: Context, obsidilithConfig: ObsidilithConfig) {
            val x = context.chunkPos().startX
            val z = context.chunkPos().startZ
            val y = obsidilithConfig.arenaGeneration.generationHeight
            val blockPos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(context.random)
            collector.addPiece(ModStructurePiece(context.structureManager, blockPos, template, rotation, ModStructures.obsidilithStructurePiece))
        }

    }

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        return getStructurePosition(
            context, Heightmap.Type.WORLD_SURFACE_WG
        ) { collector: StructurePiecesCollector? -> addPieces(collector as StructurePiecesCollector, context, obsidilithConfig) }
    }

    override fun getType(): net.minecraft.structure.StructureType<*> {
        return ModStructures.obsidilithStructureRegistry.structureTypeKey
    }
}