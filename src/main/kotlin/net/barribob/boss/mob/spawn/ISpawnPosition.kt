package net.barribob.boss.mob.spawn

import net.minecraft.util.math.Vec3d

fun interface ISpawnPosition {
    fun getPos(): Vec3d
}