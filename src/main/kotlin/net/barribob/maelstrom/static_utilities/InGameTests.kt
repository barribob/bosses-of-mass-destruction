package net.barribob.maelstrom.static_utilities

import net.minecraft.entity.Entity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

class InGameTests(private val debugPoints: DebugPointsNetworkHandler) {
    fun lineCallback(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val direction = entity.rotationVector.multiply(3.0)
        val linePoints = mutableListOf<Vec3d>()
        val pos = entity.getCameraPosVec(1f)
        MathUtils.lineCallback(pos, pos.add(direction), 10) { v, _ -> linePoints.add(v) }
        debugPoints.drawDebugPoints(linePoints, 1, pos, source.world)
    }

    fun circleCallback(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val direction = entity.rotationVector
        val linePoints = mutableListOf<Vec3d>()
        val pos = entity.getCameraPosVec(1f)
        MathUtils.circleCallback(2.0, 7, direction) { linePoints.add(it.add(pos)) }
        debugPoints.drawDebugPoints(linePoints, 1, pos, source.world)
    }

    fun boxCorners(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val box = entity.boundingBox
        debugPoints.drawDebugPoints(box.corners(), 40, entity.pos, source.world)
    }

    fun willBoxFit(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val look = entity.rotationVector
        val boxSize = 0.5
        val lookOffset = look.multiply(3.0)
        val minVec = entity.getCameraPosVec(1.0f).add(lookOffset)
        val box = Box(minVec, minVec.add(VecUtils.unit.multiply(boxSize))).offset(VecUtils.unit.multiply(-boxSize * 0.5))
        val scanDistance = look.multiply(3.0)

        MathUtils.willBoxFit(box, scanDistance) { b ->
            val fits = entity.world.isSpaceEmpty(entity, b)
            val color = if (fits) listOf(1f, 1f, 1f, 1f) else listOf(1f, 0f, 0f, 1f)
            debugPoints.drawDebugPoints(b.corners(), 1, entity.pos, source.world, color)
            return@willBoxFit !fits
        }
    }

    fun raycast(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val (pos, target) = getLineOffsets(entity)

        val blockCollision = entity.world.raycast(RaycastContext(pos, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, entity))
        val collided = blockCollision.type != HitResult.Type.MISS

        val color = if (!collided) listOf(1f, 1f, 1f, 1f) else listOf(1f, 0f, 0f, 1f)
        val linePoints = mutableListOf<Vec3d>()
        MathUtils.lineCallback(pos, target, 100) { v, _ -> linePoints.add(v) }
        debugPoints.drawDebugPoints(linePoints, 1, pos, source.world, color)
    }

    private fun getLineOffsets(entity: Entity, offset: Double = 3.0): Pair<Vec3d, Vec3d> {
        val look = entity.rotationVector
        val lookOffset = look.multiply(offset)
        val pos = entity.pos.add(lookOffset)
        val target = pos.add(lookOffset)
        return Pair(pos, target)
    }
}