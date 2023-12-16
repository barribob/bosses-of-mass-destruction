package net.barribob.maelstrom.general.math

import net.barribob.maelstrom.static_utilities.rotateVector
import net.barribob.maelstrom.static_utilities.unsignedAngle
import net.minecraft.util.math.Vec3d

class ReferencedAxisRotator(originalAxis: Vec3d, newAxis: Vec3d) {
    private val angleBetween = originalAxis.unsignedAngle(newAxis)
    private val rotationAxis: Vec3d = originalAxis.crossProduct(newAxis)

    fun rotate(vec: Vec3d): Vec3d {
        return vec.rotateVector(rotationAxis, angleBetween)
    }
}