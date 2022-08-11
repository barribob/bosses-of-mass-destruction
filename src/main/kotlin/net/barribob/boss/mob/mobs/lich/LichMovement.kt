package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.ValidatedTargetSelector
import net.barribob.boss.mob.ai.VelocitySteering
import net.barribob.boss.mob.ai.goals.VelocityGoal
import net.barribob.boss.mob.ai.valid_direction.CanMoveThrough
import net.barribob.boss.mob.ai.valid_direction.InDesiredRange
import net.barribob.boss.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.addVelocity
import net.barribob.maelstrom.static_utilities.newVec3d
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.math.Vec3d

class LichMovement(val entity: LichEntity) {
    private val reactionDistance = 4.0
    private val idleWanderDistance = 50.0
    private val iEntity = EntityAdapter(entity)
    private val tooFarFromTargetDistance = 30.0
    private val tooCloseToTargetDistance = 15.0

    fun buildAttackMovement(): VelocityGoal {
        val tooCloseToTarget: (Vec3d) -> Boolean =
            getWithinDistancePredicate(tooCloseToTargetDistance) { entity.safeGetTargetPos() }
        val tooFarFromTarget: (Vec3d) -> Boolean =
            { !getWithinDistancePredicate(tooFarFromTargetDistance) { entity.safeGetTargetPos()}(it) }
        val movingToTarget: (Vec3d) -> Boolean = { MathUtils.movingTowards(entity.safeGetTargetPos(), entity.pos, it) }

        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(entity, reactionDistance),
                InDesiredRange(tooCloseToTarget, tooFarFromTarget, movingToTarget)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            iEntity,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveWhileAttacking,
            createSteering(),
            targetSelector
        )
    }

    private fun moveWhileAttacking(velocity: Vec3d) {
        entity.addVelocity(velocity)

        val target = entity.target
        if (target != null) {
            entity.lookControl.lookAt(target.pos)
            entity.lookAtTarget(target.pos, entity.maxLookYawChange.toFloat(), entity.maxLookPitchChange.toFloat())
        }
    }

    fun buildWanderGoal(): VelocityGoal {
        val tooFarFromTarget: (Vec3d) -> Boolean = getWithinDistancePredicate(idleWanderDistance) { entity.idlePosition }
        val movingTowardsIdleCenter: (Vec3d) -> Boolean = { MathUtils.movingTowards(entity.idlePosition, entity.pos, it) }
        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(entity, reactionDistance),
                InDesiredRange({ false }, tooFarFromTarget, movingTowardsIdleCenter)
            )
        )
        val targetSelector = ValidatedTargetSelector(
            iEntity,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return VelocityGoal(
            ::moveTowards,
            createSteering(),
            targetSelector
        )
    }

    private fun createSteering() =
        VelocitySteering(iEntity, entity.getAttributeValue(EntityAttributes.GENERIC_FLYING_SPEED), 120.0)

    private fun getWithinDistancePredicate(distance: Double, targetPos: () -> Vec3d): (Vec3d) -> Boolean = {
        val target = entity.pos.add(it.multiply(reactionDistance))
        MathUtils.withinDistance(target, targetPos(), distance)
    }

    private fun moveTowards(velocity: Vec3d) {
        entity.addVelocity(velocity)

        val lookTarget = entity.pos.add(newVec3d(y = entity.standingEyeHeight.toDouble())).add(velocity)
        entity.lookControl.lookAt(lookTarget)
        entity.lookAtTarget(lookTarget, entity.maxLookYawChange.toFloat(), entity.maxLookPitchChange.toFloat())
    }
}