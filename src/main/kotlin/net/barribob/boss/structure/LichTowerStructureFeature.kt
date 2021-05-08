package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.gen.ChunkRandom
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import net.minecraft.world.gen.feature.StructureFeature.StructureStartFactory

class LichTowerStructureFeature(codec: Codec<DefaultFeatureConfig>) :
    StructureFeature<DefaultFeatureConfig>(codec) {
    override fun getStructureStartFactory(): StructureStartFactory<DefaultFeatureConfig> {
        return StructureStartFactory { feature: StructureFeature<DefaultFeatureConfig>, chunkX: Int, chunkZ: Int, box: BlockBox, references: Int, seed: Long ->
            Start(
                feature,
                chunkX,
                chunkZ,
                box,
                references,
                seed
            )
        }
    }

    override fun shouldStartAt(
        chunkGenerator: ChunkGenerator,
        biomeSource: BiomeSource,
        l: Long,
        chunkRandom: ChunkRandom?,
        i: Int,
        j: Int,
        biome: Biome?,
        chunkPos: ChunkPos?,
        defaultFeatureConfig: DefaultFeatureConfig?
    ): Boolean = VanillaCopies.shouldStartAt(this, chunkGenerator, biomeSource, i, j)

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>,
        chunkX: Int,
        chunkZ: Int,
        box: BlockBox,
        references: Int,
        seed: Long
    ) : StructureStart<DefaultFeatureConfig>(feature, chunkX, chunkZ, box, references, seed) {

        override fun init(
            registryManager: DynamicRegistryManager,
            chunkGenerator: ChunkGenerator,
            manager: StructureManager,
            chunkX: Int,
            chunkZ: Int,
            biome: Biome,
            config: DefaultFeatureConfig
        ) {
            val x = chunkX * 16 + 15
            val z = chunkZ * 16 + 15
            val y = chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG) - 7
            val pos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(random)
            children.add(
                ModPiece(
                    manager,
                    pos,
                    Mod.identifier("lich_tower_1"),
                    rotation,
                    ModStructures.lichTowerPiece
                )
            )
            children.add(
                ModPiece(
                    manager,
                    pos.up(59 - 11),
                    Mod.identifier("lich_tower_2"),
                    rotation,
                    ModStructures.lichTowerPiece
                )
            )
            setBoundingBoxFromChildren()
        }
    }
}