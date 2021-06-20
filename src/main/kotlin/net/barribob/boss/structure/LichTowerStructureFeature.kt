package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.HeightLimitView
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
        return StructureStartFactory { feature: StructureFeature<DefaultFeatureConfig>, chunkPos: ChunkPos, references: Int, seed: Long ->
            Start(
                feature,
                chunkPos,
                references,
                seed
            )
        }
    }

    override fun shouldStartAt(
        chunkGenerator: ChunkGenerator,
        biomeSource: BiomeSource,
        worldSeed: Long,
        random: ChunkRandom?,
        pos: ChunkPos,
        biome: Biome?,
        chunkPos: ChunkPos?,
        config: DefaultFeatureConfig?,
        world: HeightLimitView?
    ): Boolean = VanillaCopies.shouldStartAt(this, chunkGenerator, biomeSource, pos.x, pos.z)

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>,
        chunkPos: ChunkPos,
        references: Int,
        seed: Long
    ) : StructureStart<DefaultFeatureConfig>(feature, chunkPos, references, seed) {

        override fun init(
            registryManager: DynamicRegistryManager?,
            chunkGenerator: ChunkGenerator,
            manager: StructureManager,
            pos: ChunkPos,
            biome: Biome?,
            config: DefaultFeatureConfig?,
            world: HeightLimitView?
        ) {
            val x = pos.x * 16
            val z = pos.z * 16
            val y = chunkGenerator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG, world) - 7
            val rotation = BlockRotation.random(random)
            val blockPos = BlockPos(x, y, z).add(BlockPos(-15, 0, -15).rotate(rotation))
            children.add(
                ModStructurePiece(
                    manager,
                    blockPos,
                    Mod.identifier("lich_tower_1"),
                    rotation,
                    ModStructures.lichTowerPiece
                )
            )
            children.add(
                ModStructurePiece(
                    manager,
                    blockPos.up(59 - 11),
                    Mod.identifier("lich_tower_2"),
                    rotation,
                    ModStructures.lichTowerPiece
                )
            )
            setBoundingBoxFromChildren()
        }
    }
}