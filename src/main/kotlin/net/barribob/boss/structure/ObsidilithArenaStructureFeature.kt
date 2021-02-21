package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.config.ObsidilithConfig
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import net.minecraft.world.gen.feature.StructureFeature.StructureStartFactory


class ObsidilithArenaStructureFeature(codec: Codec<DefaultFeatureConfig>, private val obsidilithConfig: ObsidilithConfig) :
    StructureFeature<DefaultFeatureConfig>(codec) {
    override fun getStructureStartFactory(): StructureStartFactory<DefaultFeatureConfig> {
        return StructureStartFactory { feature: StructureFeature<DefaultFeatureConfig>, chunkX: Int, chunkZ: Int, box: BlockBox, references: Int, seed: Long ->
            Start(
                feature,
                chunkX,
                chunkZ,
                box,
                references,
                seed,
                obsidilithConfig
            )
        }
    }

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>,
        chunkX: Int,
        chunkZ: Int,
        box: BlockBox,
        references: Int,
        seed: Long,
        private val obsidilithConfig: ObsidilithConfig
    ) : StructureStart<DefaultFeatureConfig>(feature, chunkX, chunkZ, box, references, seed) {
        private val template: Identifier = Mod.identifier("obsidilith_arena")

        override fun init(
            registryManager: DynamicRegistryManager,
            chunkGenerator: ChunkGenerator,
            manager: StructureManager,
            chunkX: Int,
            chunkZ: Int,
            biome: Biome,
            config: DefaultFeatureConfig
        ) {
            val x = chunkX * 16
            val z = chunkZ * 16
            val y = obsidilithConfig.arenaGeneration.generationHeight
            val pos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(random)
            children.add(ModPiece(manager, pos, template, rotation))
            setBoundingBoxFromChildren()
        }
    }
}