package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.HeightLimitView
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import net.minecraft.world.gen.feature.StructureFeature.StructureStartFactory

class GauntletArenaStructureFeature(codec: Codec<DefaultFeatureConfig>) :
    StructureFeature<DefaultFeatureConfig>(codec) {
    override fun getStructureStartFactory(): StructureStartFactory<DefaultFeatureConfig> {
        return StructureStartFactory { feature: StructureFeature<DefaultFeatureConfig>, chunkPos: ChunkPos, references: Int, seed: Long ->
            Start(
                feature,
                chunkPos,
                references,
                seed
            )
        }
    }

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>,
        chunkPos: ChunkPos,
        references: Int,
        seed: Long
    ) : StructureStart<DefaultFeatureConfig>(feature, chunkPos, references, seed) {
        private val template: Identifier = Mod.identifier("gauntlet_arena")

        override fun init(
            registryManager: DynamicRegistryManager?,
            chunkGenerator: ChunkGenerator?,
            manager: StructureManager,
            pos: ChunkPos,
            biome: Biome?,
            config: DefaultFeatureConfig?,
            world: HeightLimitView?
        ) {
            val x = pos.x * 16
            val z = pos.z * 16
            val y = 15
            val blockPos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(random)
            children.add(ModPiece(manager, blockPos, template, rotation, ModStructures.gauntletArenaPiece))
            setBoundingBoxFromChildren()
        }
    }
}