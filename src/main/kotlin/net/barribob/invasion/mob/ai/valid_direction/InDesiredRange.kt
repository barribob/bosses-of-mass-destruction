package net.barribob.invasion.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d

class InDesiredRange(
    val tooClose: (Vec3d) -> Boolean,
    val tooFar: (Vec3d) -> Boolean,
    val movingCloser: (Vec3d) -> Boolean
) : IValidDirection {
    override fun isValidDirection(normedDirection: Vec3d): Boolean {
        val isTooClose = tooClose(normedDirection)
        val isMovingCloser = movingCloser(normedDirection)
        val isTooFar = tooFar(normedDirection)

        return (isTooClose && !isMovingCloser) ||
                (isTooFar && isMovingCloser) ||
                (!isTooFar && !isTooClose)
    }
}