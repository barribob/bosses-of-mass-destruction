package net.barribob.invasion.utils

import net.minecraft.util.math.Vec3d

interface IVelPos {
    fun getVel(): Vec3d
    fun getPos(): Vec3d
}