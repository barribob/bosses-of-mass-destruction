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
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Jumping AI by Barribob
 *
 * What it does
 * Detects gaps and compulsively makes entities jump over them if they are in the general direction of the target
 * Will detect water, lava and fire as well.
 *
 * What it does not do
 * Does not employ any actual path finding, so it's not a true jumping navigation ai
 * Thus it can't do complex parkour in order to get to a target
 *
 * Known Issues
 * Spider navigation makes so that spiders speed off into a straight direction
 * Jump calculations start to overestimate the distance with high velocities... mostly because minecraft has a strangely high air resistance effect going on
 */
class JumpToTargetGoal(private val entity: MobEntity) : IGoal {
    private val minTargetDistance = 1.5 // Minimum distance required for the jump ai to activate
    private val jumpClearanceAboveHead = 1.0 // Y offset above an entity's hitbox to raycast to see if there are any blocks in the way of the jump
    private val forwardMovementTicks = 40 // How many ticks the entity will "press the forward key" while jumping
    private val anglesToAttemptJump = (-45..45 step 5).toList()
    private val edgeDetectionDistance = 2.0 // Maximum distance an entity can from an edge before the ai considers running
    private val detectionPoints = floor(edgeDetectionDistance * 8).toInt()
    private val moveSpeed = 1.0
    private val jumpForwardSpeed = 5.0
    private val gravity = 0.1
    private val yVelocityScale = 1.53
    private val jumpNoise = 0.1
    private var jumpData: JumpData? = null

    data class JumpData(val jumpVel: Pair<Double, Double>, val direction: Vec3d, val edgePos: Vec3d)

    override fun getControls(): EnumSet<IGoal.Control>? {
        return EnumSet.of(IGoal.Control.MOVE, IGoal.Control.JUMP)
    }

    override fun canStart(): Boolean {
        if (entity.navigation != null && entity.isOnGround && jumpData == null) {
            val path = entity.navigation.currentPath ?: return false

            if(entity.navigation.isFollowingPath && path.reachesTarget() && path.nodes.map { getNode(it.pos) }.none { it == BlockType.PASSABLE_OBSTACLE || it == BlockType.SOLID_OBSTACLE }) {
                return false
            }

            val target = entity.navigation.targetPos?.asVec3d()?.add(0.5, 0.0, 0.5)

            if(target == null || target.distanceTo(entity.pos) < minTargetDistance) {
                return false
            }

            val jumpData = findJump(target)

            if(jumpData != null) {
                this.jumpData = jumpData
                return true
            }
        }
        return jumpData != null
    }

    private fun findJump(target: Vec3d): JumpData? {
        val targetDirection: Vec3d = target.subtract(entity.pos).planeProject(newVec3d(y = 1.0)).normalize()

        for(angle in anglesToAttemptJump) {
            val jumpDirection = targetDirection.rotateVector(newVec3d(y = 1.0), angle.toDouble())
            val gaps = mutableListOf<Pair<Vec3d, BlockType>>()
            val endPos = entity.pos.add(jumpDirection.multiply(edgeDetectionDistance))

            if(entity.velocity.add(jumpDirection).lengthSquared() < jumpDirection.lengthSquared()) {
                continue
            }

            VecUtils.lineCallback(entity.pos, endPos, detectionPoints) { pos, _ -> gaps.add(Pair(pos, getNode(BlockPos(pos)))) }

            val pairs = gaps.zipWithNext().firstOrNull { it.first.second == BlockType.WALKABLE && it.second.second == BlockType.PASSABLE_OBSTACLE }
            val hasGapsInARow = gaps.zipWithNext().firstOrNull { it.first.second == BlockType.WALKABLE && it.second.second == BlockType.WALKABLE }

            if(pairs != null && hasGapsInARow != null) {
                val dirAndVel = getJumpLength(pairs.second.first, jumpDirection)

                if (dirAndVel != null) {
                    return JumpData(dirAndVel.second, dirAndVel.first, pairs.second.first)
                }
            }
        }

        return null
    }

    override fun tick() {
        val jumpData = jumpData ?: return

        if(BlockPos(this.entity.pos) != BlockPos(jumpData.edgePos)) {
            entity.moveControl.moveTo(jumpData.edgePos.x, jumpData.edgePos.y, jumpData.edgePos.z, moveSpeed)
            return
        }

        MobUtils.leapTowards(entity, entity.pos.add(jumpData.direction), jumpData.jumpVel.first + jumpData.jumpVel.first * RandomUtils.double(jumpNoise), if(jumpData.jumpVel.second > 0) 0.0 else 0.1)
        if(jumpData.jumpVel.second > 0) {
            entity.jumpControl.setActive()
        }
        for (i in 0..forwardMovementTicks) {
            MaelstromMod.serverEventScheduler.addEvent(
                    { !entity.isAlive || entity.isOnGround },
                    {
                        val movePos = entity.pos.add(jumpData.direction.multiply(3.0))
                        entity.moveControl.moveTo(movePos.x, movePos.y, movePos.z, jumpForwardSpeed)
                    }, i)
        }
        entity.navigation.stop()
        this.jumpData = null
    }

    private fun getMobJumpAbilities(): Triple<Double, Int, Double>  {
        val jumpYVel = MobUtils.getJumpVelocity(entity.world, entity) // Maximum y velocity for a jump. Used in determining if an entity can make a jump
        val maxJumpHeight = (jumpYVel * 4).toInt()
        val maxHorizonalVelocity = entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * moveSpeed
        return Triple(jumpYVel, maxJumpHeight, maxHorizonalVelocity)
    }

    private fun getJumpOffsets(maxHorzVel: Double): List<Pair<Int, Int>> {
        val steps: Int = when {
            maxHorzVel < 0.24 -> 3
            maxHorzVel > 0.24 && maxHorzVel < 0.3 -> 4
            else -> 5
        }
        return (0..steps).flatMap { d -> (0..(steps - d)).map { y -> Pair(d, y) } }.sortedBy { pair -> pair.first + pair.second }
    }

    private fun getJumpLength(actorPos: Vec3d, targetDirection: Vec3d): Pair<Vec3d, Pair<Double, Double>>? {

        val (maxYVel, maxJumpHeight, maxHorzVel) = getMobJumpAbilities()
        val jumpOffsets = getJumpOffsets(maxHorzVel)

        for ((x, y) in jumpOffsets) {
            val scaledStepX = 1.0 + x
            val jumpToPos = actorPos.add(targetDirection.multiply(scaledStepX))
            val blockPos = BlockPos(jumpToPos)
            val groundHeight = findGroundAt(blockPos, y, maxJumpHeight) ?: continue

            val walkablePos = BlockPos(blockPos.x, groundHeight, blockPos.z)
            val blockShape = entity.world.getBlockState(walkablePos).getCollisionShape(entity.world, walkablePos)
            val offsetPos = actorPos.subtract(walkablePos.asVec3d())
            val cornerPos = MathUtils.findClosestCorner(offsetPos, blockShape, 16)?.add(walkablePos.asVec3d()) ?: continue
            val horizontalJumpPos = Vec3d(cornerPos.x, actorPos.y, cornerPos.z)

            val jumpLength = horizontalJumpPos.subtract(actorPos).length() - (entity.width * 0.5)
            val recalculatedDirection = horizontalJumpPos.subtract(actorPos).normalize()

            if (!hasClearance(actorPos, jumpLength, targetDirection)) return null

            val blockHeight = if(!blockShape.isEmpty) blockShape.boundingBox.yLength else 0.0
            val jumpHeight = groundHeight + blockHeight - actorPos.y

            for(jumpEffort in listOf(0.0, maxYVel)) {
                val xVelWithJump = calculateRequiredXVelocity(jumpLength, jumpHeight, jumpEffort)

                if(xVelWithJump < maxHorzVel) {
                    return Pair(recalculatedDirection, Pair(xVelWithJump, jumpEffort))
                }
            }
            return null
        }
        return null
    }

    private fun calculateRequiredXVelocity(jumpLength: Double, jumpHeight: Double, yVel: Double): Double {
        val scaledYVel = yVel * yVelocityScale
        val quadraticSqrt = sqrt(scaledYVel.pow(2) - 4 * - jumpHeight * -gravity)
        if(quadraticSqrt.isNaN()) return Double.POSITIVE_INFINITY
        val numerator = if(-scaledYVel > 0) -scaledYVel + quadraticSqrt else -scaledYVel - quadraticSqrt
        val time = numerator / (2 * -gravity)
        if(time == 0.0) return Double.POSITIVE_INFINITY
        return jumpLength / time
    }

    /**
     * Finds the first y position that has walkable ground or none
     */
    private fun findGroundAt(pos: BlockPos, height: Int, maxJumpHeight: Int): Int? {
        val range = (-height..maxJumpHeight)
        val walkablePos = range.firstOrNull{ getNode(pos.up(it)) == BlockType.WALKABLE }
        return if(walkablePos == null) null else walkablePos + pos.y - 1
    }

    /**
     * Finds if there are any blocks above the entity that may block the jump
     */
    private fun hasClearance(actorPos: Vec3d, jumpLength: Double, jumpDirection: Vec3d): Boolean {
        val requiredHeight = entity.height + jumpClearanceAboveHead
        val start = actorPos.yOffset(requiredHeight)
        val end = start.add(jumpDirection.multiply(jumpLength))
        val result = entity.world.rayTrace(RayTraceContext(start, end, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, entity))
        return result.type == HitResult.Type.MISS
    }

    private fun getNode(pos: BlockPos): BlockType {
        return MobUtils.getBlockType(entity.world, pos, 2)
    }
}
