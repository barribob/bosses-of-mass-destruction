package net.barribob.invasion.mob.utils

import net.minecraft.util.math.Vec3d

interface IEntity {
    fun getVel(): Vec3d
    fun getPos(): Vec3d
    fun getRotationVector(): Vec3d
    fun getAge(): Int
    fun isAlive(): Boolean
    fun target(): IEntity?
}