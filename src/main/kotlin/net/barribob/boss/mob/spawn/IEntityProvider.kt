package net.barribob.boss.mob.spawn

import net.minecraft.entity.Entity

fun interface IEntityProvider {
    fun getEntity(): Entity?
}