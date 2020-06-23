package net.barribob.maelstrom.general

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import kotlin.math.pow
import kotlin.math.sqrt

object MathUtils {
    /**
     * Treats input as a vector and finds the length of that vector
     */
    fun magnitude(vararg values: Double): Double {
        var sum = 0.0
        for (value in values) {
            sum += value.pow(2.0)
        }
        return sqrt(sum)
    }

    fun findClosestCorner(point: Vec3d, shape: VoxelShape, maxSamples: Int): Vec3d? {
        val corners = shape.boundingBoxes.flatMap { getCorners(it) }.shuffled().take(maxSamples)
        return corners.minBy { it.squaredDistanceTo(point) }
    }

    private fun getCorners(box: Box): List<Vec3d> {
        return listOf(
                Vec3d(box.minX, box.minY, box.minZ),
                Vec3d(box.maxX, box.minY, box.minZ),
                Vec3d(box.minX, box.maxY, box.minZ),
                Vec3d(box.minX, box.minY, box.maxZ),
                Vec3d(box.maxX, box.maxY, box.minZ),
                Vec3d(box.maxX, box.minY, box.maxZ),
                Vec3d(box.minX, box.maxY, box.maxZ),
                Vec3d(box.maxX, box.maxY, box.maxZ))
    }
}