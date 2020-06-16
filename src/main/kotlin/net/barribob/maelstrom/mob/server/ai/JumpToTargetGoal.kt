package net.barribob.maelstrom.mob.server.ai

import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.adapters.IGoal
import net.barribob.maelstrom.general.*
import net.barribob.maelstrom.mob.MobUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.mob.MobEntityWithAi
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RayTraceContext
import java.util.*
import kotlin.math.pow

/**
 * Jumping AI
 *
 * What it does
 *
 * Detects gaps and compulsively makes entities jump over them if they are in the general direction of the target
 *
 * Will detect water, lava and fire as well.
 *
 * What it does not do
 *
 * Does not employ any actual path finding, so it's not a true jumping navigation ai
 *
 * Issues:
 * Getting this ai to apply not just to entities with targets
 */
class JumpToTargetGoal(val entity: MobEntityWithAi, private val maxHorizonalVelocity: Double) : IGoal {

    private val minGapSize = 2
    private val minEntityDistance = 2.5
    var timeHasTarget = 0
    private val minJumpVel = 0.4
    private val targetAquireDelay = 5
    private val failureValue = -1.0
    private val yVelMax = 0.5

    override fun getControls(): EnumSet<IGoal.Control>? {
        return EnumSet.of(IGoal.Control.MOVE)
    }

    override fun canStart(): Boolean {

        if(entity.target != null) {
            timeHasTarget++
        }
        else {
            timeHasTarget = 0
        }

        if (timeHasTarget > targetAquireDelay && entity.navigation != null && entity.isOnGround && entity.distanceTo(entity.target) > minEntityDistance) {
            val target: LivingEntity = entity.target ?: return false
            val path = entity.navigation.currentPath
            if(entity.navigation.isFollowingPath && path != null && path.reachesTarget() && path.nodes.none { isDanger(it.type) }) {
                return false
            }

            val targetDirection: Vec3d = target.pos.subtract(entity.pos).planeProject(newVec3d(y = 1.0)).normalize()
            for(angle in listOf(0, 15, -15, 30, -30, 45, -45)) {

                val maxGapDistance = 1.2
                var gapStart = 0
                val endPos = entity.pos.add(targetDirection.multiply(maxGapDistance))
                val endPoint = 5
                VecUtils.lineCallback(endPos, entity.pos, endPoint) { pos, _ ->
                    run {
                        if(hasObstacle(BlockPos(pos), minGapSize)) {
                            gapStart += 1
                        }
                    }
                }

                if(gapStart in 1 until endPoint && !entity.moveControl.isMoving) {
                    entity.moveControl.moveTo(endPos.x, endPos.y, endPos.z, 1.0)
                }

                val direction = targetDirection.rotateVector(newVec3d(y = 1.0), angle.toDouble())

                if(hasObstacle(BlockPos(entity.pos), minGapSize) && tryToJump(direction)) {
                    for (i in 0..4) {
                        MaelstromMod.serverEventScheduler.addEvent(
                                { !entity.isAlive || entity.isOnGround },
                                {
                                    val movePos = entity.pos.add(direction)
                                    entity.moveControl.moveTo(movePos.x, movePos.y, movePos.z, 1.0)
                                }, i)
                    }
                    entity.navigation.stop()
                    return true
                }
            }
        }
        return false
    }

    private fun tryToJump(targetDirection: Vec3d): Boolean {
        val jumpVel = getJumpLength(entity.pos, targetDirection)
        if (jumpVel < 0) {
            return false
        }

        MobUtils.leapTowards(entity, entity.pos.add(targetDirection), jumpVel, jumpVel.coerceAtMost(yVelMax))
        return true
    }

    private fun getJumpLength(actorPos: Vec3d, targetDirection: Vec3d): Double {
        val steps = 4

        // Build the ground detection to reflect a staircase shape in this order:
        // 0 1 3
        // 2 4
        // 5
        // etc...
        val heightDepthPairs = (0..steps).flatMap { d -> (0..(steps - d)).map { y -> Pair(d, y) } }.sortedBy { pair -> pair.first + pair.second }
        for ((x, y) in heightDepthPairs) {
            val scaledStep = 2 + x + 0.5
            val jumpToPos = actorPos.add(targetDirection.multiply(scaledStep))
            val blockPos = BlockPos(jumpToPos)
            val groundHeight = findGroundAt(blockPos, y)
            val walkablePos = BlockPos(blockPos.x, groundHeight, blockPos.z)

            if (groundHeight == failureValue.toInt()) {
                continue
            }

            var jumpLength = calculateJumpDistance(walkablePos, actorPos, jumpToPos, groundHeight.toDouble())
            if (jumpLength == failureValue) {
                continue
            }

            jumpLength -= (entity.width * 0.5)

            if (!hasClearance(jumpLength, targetDirection, 1.0)) {
                return failureValue
            }

            val blockHeight = entity.world.getBlockState(walkablePos).getCollisionShape(entity.world, walkablePos).boundingBox.yLength
            val jumpHeight = groundHeight + blockHeight - actorPos.y
            val verticalImpact = 0.33
            val gravity = 0.25
            val requiredJumpVel = (jumpLength.pow(2.0) * gravity + verticalImpact * jumpHeight) / jumpLength
            val angledMaxJumpVel = MathUtils.magnitude(yVelMax, maxHorizonalVelocity)
            if (requiredJumpVel < angledMaxJumpVel) {
                return if (requiredJumpVel < minJumpVel) minJumpVel else requiredJumpVel
            }
            return failureValue
        }
        return failureValue
    }

    private fun calculateJumpDistance(walkablePos: BlockPos, actorPos: Vec3d, jumpToPos: Vec3d, groundHeight: Double): Double {
        val solidBlockPos = walkablePos.down()

        var blockShape = entity.world.getBlockState(solidBlockPos).getRayTraceShape(entity.world, solidBlockPos).offset(0.0, actorPos.y - groundHeight, 0.0)
        if (blockShape.isEmpty) {
            blockShape = net.minecraft.block.Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
                    .offset(solidBlockPos.x.toDouble(), actorPos.y - 1, solidBlockPos.z.toDouble())
        }

        val start = actorPos.yOffset(-0.5)
        val end = jumpToPos.yOffset(-0.5)
        val result = blockShape.boundingBox.rayTrace(start, end)

        if (result.isPresent) {
            return result.get().subtract(actorPos).length()
        }

        return failureValue
    }

    private fun findGroundAt(pos: BlockPos, height: Int): Int {
        val range = (-height..1)
        val walkablePos = range.firstOrNull{ getNode(pos.up(it)) == PathNodeType.WALKABLE }
        return if(walkablePos == null) failureValue.toInt() else walkablePos + pos.y - 1
    }

    private fun hasClearance(jumpLength: Double, jumpDirection: Vec3d, heightAbove: Double): Boolean {
        val requiredHeight = entity.height + heightAbove
        val start = entity.pos.yOffset(requiredHeight)
        val end = start.add(jumpDirection.multiply(jumpLength))
        val result = entity.world.rayTrace(RayTraceContext(start, end, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, entity))
        return result.type == HitResult.Type.MISS
    }

    private fun hasObstacle(startPos: BlockPos, blocksBelow: Int): Boolean {
        val range = (0 downTo - (blocksBelow - 1))
        val isOpenInFront = getNode(startPos.up()) == PathNodeType.OPEN
        val isOpenAtBottom = getNode(startPos.down(blocksBelow)) == PathNodeType.OPEN || getNode(startPos.down(blocksBelow)) == PathNodeType.WALKABLE
        val isOpenGap = range.all { getNode(startPos.up(it)) == PathNodeType.OPEN }

        if(isOpenGap && isOpenAtBottom && isOpenInFront) {
            return true
        }

        val obstaclePos = range.firstOrNull { isDanger(getNode(startPos.up(it))) }
        if(obstaclePos != null) {
            val isWalkableAboveObstacle = (0 downTo (obstaclePos + 1)).any { getNode(startPos.up(it)) == PathNodeType.WALKABLE }
            return !isWalkableAboveObstacle
        }

        return false
    }

    private fun getNode(pos: BlockPos): PathNodeType {
        val nodeMaker = entity.navigation.nodeMaker
        return nodeMaker.getDefaultNodeType(entity.world, pos.x, pos.y, pos.z)
    }
    
    private fun isDanger(node: PathNodeType): Boolean {
        return node == PathNodeType.DAMAGE_FIRE ||
            node == PathNodeType.LAVA ||
            node == PathNodeType.WATER ||
            node == PathNodeType.DANGER_OTHER ||
            node == PathNodeType.DANGER_FIRE
    }
}
