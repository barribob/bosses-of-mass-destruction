package net.barribob.invasion.testing_utilities

import net.barribob.invasion.utils.IVelPos
import net.minecraft.util.math.Vec3d

class StubVelPos(private val velocity: Vec3d = Vec3d.ZERO, private val position: Vec3d = Vec3d.ZERO) : IVelPos {
    override fun getVel(): Vec3d = velocity
    override fun getPos(): Vec3d = position
}