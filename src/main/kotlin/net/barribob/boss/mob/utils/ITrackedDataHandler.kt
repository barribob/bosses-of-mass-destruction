package net.barribob.boss.mob.utils

import net.minecraft.entity.data.TrackedData

interface ITrackedDataHandler {
    fun onTrackedDataSet(data: TrackedData<*>)
}