package net.barribob.boss.mob.utils

import net.minecraft.world.World

class CompositeTickHandler<T: World>(vararg tickHandlers: IEntityTick<T>): IEntityTick<T> {
    private val handlerList = tickHandlers.toList()

    override fun tick(world: T) {
        handlerList.forEach { it.tick(world) }
    }
}