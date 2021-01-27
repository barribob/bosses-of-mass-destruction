package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.barribob.boss.Mod
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World

class ModComponents : WorldComponentInitializer, EntityComponentInitializer {
    companion object : IWorldEventScheduler, ILichSummonCounter {
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

        override fun getWorldEventScheduler(world: World) = eventSchedulerComponentKey.get(world).get()
        override fun getLichSummons(playerEntity: PlayerEntity): Int = lichSummonCounterComponentKey.get(playerEntity).getValue()
        override fun increment(playerEntity: PlayerEntity) {
            lichSummonCounterComponentKey.get(playerEntity).increment()
        }
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(
            eventSchedulerComponentKey,
            ServerWorldEventScheduler::class.java,
            ::ServerWorldEventScheduler
        )
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(PlayerEntity::class.java, lichSummonCounterComponentKey, ::LichSummonCounter)
    }
}