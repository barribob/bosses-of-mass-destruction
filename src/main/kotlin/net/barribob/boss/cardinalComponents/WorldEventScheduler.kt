package net.barribob.boss.cardinalComponents

import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.World

class WorldEventScheduler(val world: World): IWorldEventSchedulerComponent {
    private val eventScheduler = EventScheduler()

    override fun get(): EventScheduler = eventScheduler
    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
    }

    override fun serverTick() {
        eventScheduler.updateEvents()
    }

    // No persistence for world event scheduler
    override fun clientTick() {
        eventScheduler.updateEvents()
    }
}