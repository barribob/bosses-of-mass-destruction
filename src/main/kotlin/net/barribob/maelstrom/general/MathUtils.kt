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
        val corners = shape.boundingBoxes.flatMap { getTopCornersAndEdges(it) }.shuffled().take(maxSamples)
        return corners.minBy { it.squaredDistanceTo(point) }
    }

    private fun getTopCornersAndEdges(box: Box): List<Vec3d> {
        val halfX = box.xLength * 0.5
        val halfZ = box.zLength * 0.5

        return listOf(
                Vec3d(box.minX, box.maxY, box.minZ),
                Vec3d(box.maxX, box.maxY, box.minZ),
                Vec3d(box.minX, box.maxY, box.maxZ),
                Vec3d(box.maxX, box.maxY, box.maxZ),
                Vec3d(box.minX + halfX, box.maxY, box.minZ),
                Vec3d(box.minX, box.maxY, box.minZ + halfZ),
                Vec3d(box.maxX, box.maxY, box.minZ + halfZ),
                Vec3d(box.minX + halfX, box.maxY, box.maxZ)
        )
    }
}