package net.barribob.invasion.mob.ai.valid_direction

import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.Entity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

class CanMoveThrough(private val entity: Entity, private val reactionDistance: Double) : IValidDirection {
    override fun isValidDirection(normedDirection: Vec3d): Boolean {
        val reactionDirection = normedDirection.multiply(reactionDistance)
        val target = entity.pos.add(reactionDirection)
        val noBlockCollisions = MathUtils.willBoxFit(entity.boundingBox, reactionDirection) { !entity.world.isSpaceEmpty(entity, it) }
        val blockCollision = entity.world.raycast(
            RaycastContext(
                entity.pos.add(normedDirection.multiply(1.0)),
                target,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.ANY,
                entity
            )
        )
        val noFluidCollisions = blockCollision.type == HitResult.Type.MISS

        // Todo: Remove when finished with animation
//        val points = mutableListOf<Vec3d>()
//        MathUtils.lineCallback(entity.pos, target, 100) { vec3d, _ -> points.add(vec3d) }
//        val color = if (noBlockCollisions && noFluidCollisions) listOf(1f, 1f, 0f, 1f) else listOf(1f, 0f, 0f, 1f)
//        ClientServerUtils.drawDebugPoints(points, 1, entity.pos, entity.world, color)
        return noFluidCollisions && noBlockCollisions
    }
}