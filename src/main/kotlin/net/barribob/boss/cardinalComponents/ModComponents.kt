package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.barribob.boss.Mod
import net.minecraft.world.World

class ModComponents : WorldComponentInitializer {
    companion object : IWorldEventScheduler{
        private val eventSchedulerComponentKey: ComponentKey<IWorldEventSchedulerComponent> =
            ComponentRegistryV3.INSTANCE.getOrCreate(
                Mod.identifier("event_scheduler"),
                IWorldEventSchedulerComponent::class.java
            )

        override fun getWorldEventScheduler(world: World) = eventSchedulerComponentKey.get(world).get()
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(
            eventSchedulerComponentKey,
            ServerWorldEventScheduler::class.java,
            ::ServerWorldEventScheduler
        )
    }
}