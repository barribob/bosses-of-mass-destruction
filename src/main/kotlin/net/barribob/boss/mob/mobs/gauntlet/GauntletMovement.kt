package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.ValidatedTargetSelector
import net.barribob.boss.mob.ai.VelocitySteering
import net.barribob.boss.mob.ai.goals.VelocityGoal
import net.barribob.boss.mob.ai.valid_direction.CanMoveThrough
import net.barribob.boss.mob.ai.valid_direction.InDesiredRange
import net.barribob.boss.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.math.Vec3d

class GauntletMovement(val entity: GauntletEntity) {
    private val reactionDistance = 4.0
    private val iEntity = EntityAdapter(entity)
    private val tooFarFromTargetDistance = 25.0
    private val tooCloseToTargetDistance = 5.0

    fun buildAttackMovement(): VelocityGoal {
        val targetPos = { entity.safeGetTargetPos()}
        val tooCloseToTarget: (Vec3d) -> Boolean = getWithinDistancePredicate(tooCloseToTargetDistance, targetPos)
        val tooFarFromTarget: (Vec3d) -> Boolean = { !getWithinDistancePredicate(tooFarFromTargetDistance, targetPos)(it) }
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
            ::moveAndLookAtTarget,
            VelocitySteering(iEntity, entity.getAttributeValue(EntityAttributes.GENERIC_FLYING_SPEED), 120.0),
            targetSelector
        )
    }

    private fun moveAndLookAtTarget(velocity: Vec3d) {
        entity.addVelocity(velocity)

        val target = entity.target
        if (target != null) {
            entity.lookControl.lookAt(target.eyePos())
            entity.lookAtEntity(target, entity.maxLookYawChange.toFloat(), entity.maxLookPitchChange.toFloat())
        }
    }

    private fun getWithinDistancePredicate(distance: Double, targetPos: () -> Vec3d): (Vec3d) -> Boolean = {
        val target = entity.pos.add(it.multiply(reactionDistance))
        MathUtils.withinDistance(target, targetPos(), distance)
    }
}