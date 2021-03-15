package net.barribob.boss.mob.utils

import net.minecraft.world.World

interface IEntityTick<T> where T : World {
    fun tick(world: T)
}