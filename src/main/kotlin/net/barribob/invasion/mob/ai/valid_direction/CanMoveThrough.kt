package net.barribob.invasion.mob.ai.valid_direction

import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.Entity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

class CanMoveThrough(private val entity: Entity, private val reactionDistance: Double) : IValidDirection {
    override fun isValidDirection(normedDirection: Vec3d): Boolean {
        val reactionDirection = normedDirection.multiply(reactionDistance).add(entity.velocity)
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

        return noFluidCollisions && noBlockCollisions
    }
}