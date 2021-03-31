package net.barribob.boss.mob.utils

import net.minecraft.entity.data.TrackedData

class CompositeTrackedDataHandler(vararg dataHandlers: ITrackedDataHandler) : ITrackedDataHandler {
    private val handlerList = dataHandlers.toList()

    override fun onTrackedDataSet(data: TrackedData<*>) {
        handlerList.forEach { it.onTrackedDataSet(data) }
    }
}