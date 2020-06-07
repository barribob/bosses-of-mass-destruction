package net.barribob.maelstrom.mob.server.ai

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntityWithAi
import net.minecraft.entity.player.PlayerEntity
import java.util.*
import kotlin.math.max
import kotlin.math.pow

/**
 * A more generalized version of the skeleton strafing bow attack ai.
 */
class TimedAttackGoal(private val mob: MobEntityWithAi, private val maxAttackDistance: Float, val idealAttackDistance: Float, private val attackCooldown: Int, val attackFunction: () -> Int, val speed: Double = 1.0, val strafeAmount: Float = 0.5F, val lookSpeed: Float = 30F) : Goal() {

    private var attackTime = attackCooldown
    private var unseeTime = 0
    private var strafingTime = -1
    private var strafingClockwise = false
    private var strafingBackwards = false
    private val memory = 100
    private val strafingStopFactor = 0.75f
    private val strafingBackwardsFactor = 0.25f
    private val strafingDirectionTick = 20f
    private val strafingDirectionChangeChance = 0.3f

    override fun canStart(): Boolean {
        val target = mob.target
        controls = EnumSet.of(Control.MOVE, Control.LOOK)
        return target != null && target.isAlive
    }

    override fun shouldContinue(): Boolean {
        val target = mob.target
        return target != null && target.isAlive &&
        (target !is PlayerEntity || (!target.isSpectator() && !target.isCreative))
    }

    override fun stop() {
        super.stop()
        this.attackTime = max(attackTime, attackCooldown)
    }

    override fun tick() {
        val target = this.mob.target ?: return

        val distSq: Double = this.mob.squaredDistanceTo(target.getCameraPosVec(1.0F))
        var canSee: Boolean = this.mob.visibilityCache.canSee(target)

        // Implements some sort of memory mechanism (can still attack a short while after the enemy isn't seen)
        if (canSee) {
            unseeTime = 0
        } else {
            unseeTime += 1
        }

        canSee = canSee || unseeTime < memory

        move(target, distSq, canSee)

        if (distSq <= maxAttackDistance.pow(2) && canSee) {
            attackTime--
            if (attackTime <= 0) {
                attackTime = this.attackFunction()
            }
        }
    }

    private fun move(target: LivingEntity, distSq: Double, canSee: Boolean) {
        if (distSq <= idealAttackDistance.pow(2) && canSee) {
            this.mob.navigation.stop()
            ++this.strafingTime
        } else {
            this.mob.navigation.startMovingTo(target, this.speed)
            this.strafingTime = -1
        }
        if (this.strafingTime >= strafingDirectionTick) {
            if (this.mob.random.nextFloat() < strafingDirectionChangeChance) {
                this.strafingClockwise = !this.strafingClockwise
            }
            if (this.mob.random.nextFloat() < strafingDirectionChangeChance) {
                this.strafingBackwards = !this.strafingBackwards
            }
            this.strafingTime = 0
        }
        if (this.strafingTime > -1) {
            if (distSq > idealAttackDistance.pow(2) * strafingStopFactor) {
                this.strafingBackwards = false
            } else if (distSq < idealAttackDistance.pow(2) * strafingBackwardsFactor) {
                this.strafingBackwards = true
            }
            this.mob.moveControl.strafeTo((if (this.strafingBackwards) -1 else 1) * strafeAmount, (if (this.strafingClockwise) 1 else -1) * strafeAmount)
            this.mob.lookAtEntity(target, lookSpeed, lookSpeed)
        } else {
            this.mob.lookControl.lookAt(target, lookSpeed, lookSpeed)
        }
    }
}