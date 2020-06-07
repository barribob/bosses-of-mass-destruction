package net.barribob.maelstrom.general

import net.minecraft.util.math.Vec3d

fun Vec3d.yOffset(i: Double): Vec3d = this.add(0.0, i, 0.0)

fun Vec3d.planeProject(planeVector: Vec3d): Vec3d = this.subtract(planeVector.multiply(this.dotProduct(planeVector)))

fun newVec3d(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Vec3d = Vec3d(x, y, z)
