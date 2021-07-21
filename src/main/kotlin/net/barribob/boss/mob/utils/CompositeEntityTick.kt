package net.barribob.boss.mob.utils

import net.minecraft.world.World

class CompositeEntityTick<T: World>(vararg tickHandlers: IEntityTick<T>) : IEntityTick<T> {
    private val tickList = tickHandlers.toList()

    override fun tick(world: T) {
        for(tickHandler in tickList) {
            tickHandler.tick(world)
        }
    }
}