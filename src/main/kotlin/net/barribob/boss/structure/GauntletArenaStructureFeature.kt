package net.barribob.boss.structure

import com.mojang.serialization.Codec
import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities
import net.barribob.boss.utils.ModStructures
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Blocks
import net.minecraft.structure.StructureManager
import net.minecraft.structure.StructureStart
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.ServerWorldAccess
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import net.minecraft.world.gen.feature.StructureFeature.StructureStartFactory
import java.util.*

class GauntletArenaStructureFeature(codec: Codec<DefaultFeatureConfig>) :
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

    class Start(
        feature: StructureFeature<DefaultFeatureConfig>,
        chunkX: Int,
        chunkZ: Int,
        box: BlockBox,
        references: Int,
        seed: Long
    ) : StructureStart<DefaultFeatureConfig>(feature, chunkX, chunkZ, box, references, seed) {
        private val template: Identifier = Mod.identifier("gauntlet_arena")

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
            val y = 15
            val pos = BlockPos(x, y, z)
            val rotation = BlockRotation.random(random)
            children.add(
                ModPiece(
                    manager,
                    pos,
                    template,
                    rotation,
                    ModStructures.gauntletArenaPiece,
                    { s: String, blockPos: BlockPos, serverWorldAccess: ServerWorldAccess, _: Random, _: BlockBox ->
                        if (s.contains("gauntlet")) {
                            val spawnPos = blockPos.asVec3d().add(VecUtils.unit.multiply(0.5))
                            val entity = Entities.GAUNTLET.create(serverWorldAccess.toServerWorld())
                            if (entity != null) {
                                entity.updateTrackedPosition(spawnPos)
                                entity.updatePosition(spawnPos.x, spawnPos.y, spawnPos.z)
                                serverWorldAccess.spawnEntity(entity)
                            }
                            serverWorldAccess.setBlockState(blockPos, Blocks.AIR.defaultState, 0)
                        }
                    })
            )
            setBoundingBoxFromChildren()
        }
    }
}