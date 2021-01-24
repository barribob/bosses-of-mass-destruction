package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.World

class ServerWorldEventScheduler(val world: World): IWorldEventSchedulerComponent, ServerTickingComponent {
    private val eventScheduler = EventScheduler()

    override fun get(): EventScheduler = eventScheduler

    override fun serverTick() {
        eventScheduler.updateEvents()
    }

    // No persistence for world event scheduler
    override fun readFromNbt(p0: CompoundTag) {
    }

    override fun writeToNbt(p0: CompoundTag) {
    }
}