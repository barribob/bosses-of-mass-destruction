package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d

fun interface IValidDirection {
    fun isValidDirection(normedDirection: Vec3d): Boolean
}