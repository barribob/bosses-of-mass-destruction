package net.barribob.maelstrom.mob.server.ai

import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.adapters.IGoal
import net.barribob.maelstrom.general.*
import net.barribob.maelstrom.mob.MobUtils
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RayTraceContext
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Jumping AI
 *
 * What it does
 * Detects gaps and compulsively makes entities jump over them if they are in the general direction of the target
 * Will detect water, lava and fire as well.
 *
 * What it does not do
 * Does not employ any actual path finding, so it's not a true jumping navigation ai
 *
 * Issues
 * Entities hesitant to jump java
 * Entities that can't make the jump might run off
 * Entities have trouble with header hitting
 */
class JumpToTargetGoal(val entity: MobEntity) : IGoal {

    private val minTargetDistance = 2 // Minimum distance required for the jump ai to activate
    private val jumpYVel = 0.5 // Maximum y velocity for a jump. Used in determining if an entity can make a jump
    private val jumpClearanceAboveHead = 1.0 // Y offset above an entity's hitbox to raycast to see if there are any blocks in the way of the jump
    private val gravity = 0.08 // Minecraft's gravity constant????
    private val forwardMovementTicks = 40 // How many ticks the entity will "press the forward key" while jumping
    private val anglesToAttemptJump = listOf(0, 15, -15, 30, -30, 45, -45)
    private val edgeDetectionDistance = 1.2 // Maximum distance an entity can from an edge before the ai considers running
    private val detectionPoints = 5
    private val moveSpeed = 1.0
    private val jumpForwardSpeed = 5.0
    private val maxHorizonalVelocity = entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * moveSpeed
    private val yVelocityScale = 1.4

    override fun getControls(): EnumSet<IGoal.Control>? {
        return EnumSet.of(IGoal.Control.MOVE)
    }

    override fun canStart(): Boolean {
        if (entity.navigation != null && entity.isOnGround) {
            val path = entity.navigation.currentPath

            // If the pathfinder has found a safe path already, no need for jumping
            if(entity.navigation.isFollowingPath && path != null && path.reachesTarget() && path.nodes.map { getNode(it.pos) }.none { it == BlockType.PASSABLE_OBSTACLE || it == BlockType.SOLID_OBSTACLE }) {
//                println("Have safe path")
                return false
            }

            val target = entity.navigation.targetPos?.asVec3d()?.add(0.5, 0.0, 0.5)

            if(target == null || target.distanceTo(entity.pos) < minTargetDistance) {
//                println("No Target or Target too close")
                return false
            }

            return tryToJump(target)
        }
        return false
    }

    private fun tryToJump(target: Vec3d): Boolean {

        // Only xz direction
        val targetDirection: Vec3d = target.subtract(entity.pos).planeProject(newVec3d(y = 1.0)).normalize()

        for(angle in anglesToAttemptJump) {
            val gaps = BooleanArray(detectionPoints) { false }
            val endPos = entity.pos.add(targetDirection.multiply(edgeDetectionDistance))

            VecUtils.lineCallback(entity.pos, endPos, detectionPoints) { pos, i ->
                run {
                    if(getNode(BlockPos(pos)) == BlockType.PASSABLE_OBSTACLE) {
                        gaps[i] = true
                    }
                }
            }

            println(gaps.joinToString())

            // If edge is farther away, move towards edge
            if(!gaps[0] && gaps.any{ it } && !entity.moveControl.isMoving) {
                entity.moveControl.moveTo(endPos.x, endPos.y, endPos.z, moveSpeed)
            }

            val direction = targetDirection.rotateVector(newVec3d(y = 1.0), angle.toDouble())

            // If on edge, attempt jump
            if(gaps[0] && gaps[1] && tryToJumpInDirection(direction)) {

                print("jumped")

                // Make entity move forward for a certain number of ticks in the future
                for (i in 0..forwardMovementTicks) {
                    MaelstromMod.serverEventScheduler.addEvent(
                            { !entity.isAlive || entity.isOnGround },
                            {
                                val movePos = entity.pos.add(direction)
                                entity.moveControl.moveTo(movePos.x, movePos.y, movePos.z, jumpForwardSpeed)
                            }, i)
                }
                entity.navigation.stop()
                println()
                return true
            }
        }
        return false
    }

    private fun tryToJumpInDirection(targetDirection: Vec3d): Boolean {
        val jumpVel = getJumpLength(entity.pos, targetDirection) ?: return false
        MobUtils.leapTowards(entity, entity.pos.add(targetDirection), jumpVel.first, jumpVel.second)
        return true
    }

    private fun getJumpLength(actorPos: Vec3d, targetDirection: Vec3d): Pair<Double, Double>? {
        val steps = 5

        // Build the ground detection to reflect a staircase shape in this order:
        // 0 1 3
        // 2 4
        // 5
        // etc...
        val heightDepthPairs = (0..steps).flatMap { d -> (0..(steps - d)).map { y -> Pair(d, y) } }.sortedBy { pair -> pair.first + pair.second }
        for ((x, y) in heightDepthPairs) {
            val scaledStepX = 1.0 + x
            val jumpToPos = actorPos.add(targetDirection.multiply(scaledStepX))
            val blockPos = BlockPos(jumpToPos)
            val groundHeight = findGroundAt(blockPos, y) ?: continue
//            print("found ground: ")
            val walkablePos = BlockPos(blockPos.x, groundHeight, blockPos.z)
            var jumpLength = calculateJumpDistance(walkablePos, actorPos, jumpToPos, groundHeight.toDouble()) ?: continue

//            print("found length: ")

            jumpLength -= (entity.width * 0.5)

            if (!hasClearance(jumpLength, targetDirection)) {
                return null
            }

//            print("has clearance: ")

            // Approximate required jump velocity using some basic physics... intuition?
            val blockShape = entity.world.getBlockState(walkablePos).getCollisionShape(entity.world, walkablePos)
            val blockHeight = if(!blockShape.isEmpty) blockShape.boundingBox.yLength else 0.0
            val jumpHeight = groundHeight + blockHeight - actorPos.y

            println("Jump Distance: $jumpLength Jump Height: $jumpHeight")

            val xVelNoJump = calculateRequiredXVelocity(jumpLength, jumpHeight, 0.0)
            println("$xVelNoJump $maxHorizonalVelocity")

            if(xVelNoJump < maxHorizonalVelocity) {
                return Pair(xVelNoJump, 0.0)
            }

            val xVelWithJump = calculateRequiredXVelocity(jumpLength, jumpHeight, jumpYVel)

            println("$xVelWithJump $maxHorizonalVelocity")
            if(xVelWithJump < maxHorizonalVelocity) {
                return Pair(xVelWithJump, jumpYVel)
            }

            return null
        }
        return null
    }

    fun calculateRequiredXVelocity(jumpLength: Double, jumpHeight: Double, yVel: Double): Double {
        val scaledYVel = yVel * yVelocityScale
        val quadraticSqrt = sqrt(scaledYVel.pow(2) - 4 * - jumpHeight * -gravity)
        if(quadraticSqrt.isNaN()) return Double.POSITIVE_INFINITY
        val numerator = if(-scaledYVel > 0) -scaledYVel + quadraticSqrt else -scaledYVel - quadraticSqrt
        val time = numerator / (2 * -gravity)
        if(time == 0.0) return Double.POSITIVE_INFINITY
        return jumpLength / time
    }

    /**
     * Uses block raycasting to determine how far the entity must jump horizontally to get to ground
     */
    private fun calculateJumpDistance(walkablePos: BlockPos, actorPos: Vec3d, jumpToPos: Vec3d, groundHeight: Double): Double? {
        var blockShape = entity.world.getBlockState(walkablePos).getRayTraceShape(entity.world, walkablePos).offset(0.0, actorPos.y - groundHeight, 0.0)
        if (blockShape.isEmpty) {
            blockShape = net.minecraft.block.Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
                    .offset(walkablePos.x.toDouble(), actorPos.y - 1, walkablePos.z.toDouble())
        }

        val start = actorPos.yOffset(-0.5)
        val end = jumpToPos.yOffset(-0.5)
        val result = blockShape.boundingBox.rayTrace(start, end)

        if (result.isPresent) {
            return result.get().subtract(actorPos).length()
        }

        return null
    }

    /**
     * Finds the first y position that has walkable ground or none
     */
    private fun findGroundAt(pos: BlockPos, height: Int): Int? {
        val range = (-height..1)
        val walkablePos = range.firstOrNull{ getNode(pos.up(it)) == BlockType.WALKABLE }
        return if(walkablePos == null) null else walkablePos + pos.y - 1
    }

    /**
     * Finds if there are any blocks above the entity that may block the jump
     */
    private fun hasClearance(jumpLength: Double, jumpDirection: Vec3d): Boolean {
        val requiredHeight = entity.height + jumpClearanceAboveHead
        val start = entity.pos.yOffset(requiredHeight)
        val end = start.add(jumpDirection.multiply(jumpLength))
        val result = entity.world.rayTrace(RayTraceContext(start, end, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, entity))
        return result.type == HitResult.Type.MISS
    }

    private fun getNode(pos: BlockPos): BlockType {
        return MobUtils.getBlockType(entity.world, pos, 2)
    }
}
