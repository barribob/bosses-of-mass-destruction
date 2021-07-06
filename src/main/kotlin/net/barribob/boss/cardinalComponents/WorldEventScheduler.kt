package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World

class WorldEventScheduler(val world: World): IWorldEventSchedulerComponent, ServerTickingComponent, ClientTickingComponent {
    private val eventScheduler = EventScheduler()

    override fun get(): EventScheduler = eventScheduler

    override fun serverTick() {
        eventScheduler.updateEvents()
    }

    // No persistence for world event scheduler
    override fun readFromNbt(p0: NbtCompound) {
    }

    override fun writeToNbt(p0: NbtCompound) {
    }

    override fun clientTick() {
        eventScheduler.updateEvents()
    }
}