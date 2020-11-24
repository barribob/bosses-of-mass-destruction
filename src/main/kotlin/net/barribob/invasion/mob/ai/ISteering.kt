package net.barribob.invasion.mob.ai

import net.minecraft.util.math.Vec3d

interface ISteering {
    fun accelerateTo(target: Vec3d): Vec3d
}