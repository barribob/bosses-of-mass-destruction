package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.mob.ai.ValidatedTargetSelector
import net.barribob.invasion.mob.ai.AerialWanderGoal
import net.barribob.invasion.mob.ai.VelocitySteering
import net.barribob.invasion.mob.ai.valid_direction.CanMoveThrough
import net.barribob.invasion.mob.ai.valid_direction.IValidDirection
import net.barribob.invasion.mob.ai.valid_direction.ValidDirectionAnd
import net.barribob.invasion.mob.ai.valid_direction.ValidDirectionOr
import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.barribob.invasion.utils.VanillaCopies
import net.barribob.invasion.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.addVelocity
import net.barribob.maelstrom.static_utilities.newVec3d
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    var velocityHistory = HistoricalData(Vec3d.ZERO)

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "skull_float", 0f, createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this, "arms_idle", 0f, createIdlePredicate("arms_idle")))
        data.addAnimationController(AnimationController(this, "book_idle", 0f, createIdlePredicate("book_idle")))
    }

    override fun initGoals() {
        goalSelector.add(1, SwimGoal(this))
        goalSelector.add(2, buildWanderGoal())
        super.initGoals()
    }

    private fun buildWanderGoal(): AerialWanderGoal {
        val reactionDistance = 4.0
        val idleWanderDistance = 25.0
        val withinDistanceValidator = IValidDirection {
            val target = pos.add(it.multiply(reactionDistance))
            MathUtils.withinDistance(target, idlePosition, idleWanderDistance)
        }
        val movingTowardsIdleCenterValidator = IValidDirection { MathUtils.movingTowards(idlePosition, pos, it) }
        val canMoveTowardsPositionValidator = ValidDirectionAnd(
            listOf(
                CanMoveThrough(this, reactionDistance),
                ValidDirectionOr(
                    listOf(
                        movingTowardsIdleCenterValidator,
                        withinDistanceValidator
                    )
                )
            )
        )
        val targetSelector = ValidatedTargetSelector(
            this,
            canMoveTowardsPositionValidator,
            ModRandom()
        )
        return AerialWanderGoal(
            ::moveTowards,
            VelocitySteering(this, 4.0, 120.0),
            targetSelector
        )
    }

    private fun moveTowards(velocity: Vec3d) {
        addVelocity(velocity)

        val lookTarget = pos.add(newVec3d(y = standingEyeHeight.toDouble())).add(velocity)
        lookControl.lookAt(lookTarget)
        lookAtTarget(lookTarget, bodyYawSpeed.toFloat(), lookPitchSpeed.toFloat())
    }

    override fun clientTick() {
        velocityHistory.set(velocity)
    }

    private fun createIdlePredicate(animationName: String): AnimationPredicate<LichEntity> = AnimationPredicate {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation(animationName, true)
        )
        PlayState.CONTINUE
    }

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?
    ) {
        this.moveControl
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this)
    }

    override fun isClimbing(): Boolean = false
}