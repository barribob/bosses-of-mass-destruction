package net.barribob.boss.render

import net.minecraft.entity.Entity

interface IRenderDataProvider<T : Entity> {
    fun provide(entity: T, partialTicks: Float)
}