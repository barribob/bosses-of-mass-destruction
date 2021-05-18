package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.barribob.boss.Mod
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.util.*

class ModComponents : WorldComponentInitializer, EntityComponentInitializer, ChunkComponentInitializer {
    companion object : IWorldEventScheduler, ILichSummonCounter, IPlayerMoveHistory {
        private val eventSchedulerComponentKey: ComponentKey<IWorldEventSchedulerComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("event_scheduler"),
                IWorldEventSchedulerComponent::class.java
            )

        private val lichSummonCounterComponentKey: ComponentKey<ILichSummonCounterComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("lich_summon_counter"),
                ILichSummonCounterComponent::class.java
            )

        private val playerMoveHistoryComponentKey: ComponentKey<IPlayerMoveHistoryComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("player_move_history"),
                IPlayerMoveHistoryComponent::class.java
            )

        private val chunkBlockCacheComponentKey: ComponentKey<IChunkBlockCacheComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("chunk_block_cache_component"),
                IChunkBlockCacheComponent::class.java
            )

        override fun getWorldEventScheduler(world: World) = eventSchedulerComponentKey.get(world).get()
        override fun getLichSummons(playerEntity: PlayerEntity): Int =
            lichSummonCounterComponentKey.get(playerEntity).getValue()

        override fun increment(playerEntity: PlayerEntity) {
            lichSummonCounterComponentKey.get(playerEntity).increment()
        }

        override fun getPlayerPositions(serverPlayerEntity: ServerPlayerEntity): List<Vec3d> =
            playerMoveHistoryComponentKey.get(serverPlayerEntity).getHistoricalPositions()

        fun getChunkBlockCache(chunk: Chunk): Optional<IChunkBlockCacheComponent> =
            chunkBlockCacheComponentKey.maybeGet(chunk)
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(
            eventSchedulerComponentKey,
            WorldEventScheduler::class.java,
            ::WorldEventScheduler
        )
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(PlayerEntity::class.java, lichSummonCounterComponentKey, ::LichSummonCounter)
        registry.registerFor(ServerPlayerEntity::class.java, playerMoveHistoryComponentKey, ::PlayerMoveHistory)
    }

    override fun registerChunkComponentFactories(registry: ChunkComponentFactoryRegistry) {
        registry.register(chunkBlockCacheComponentKey, ::ChunkBlockCacheComponent)
    }
}