package net.barribob.invasion.utils

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.mob.FlyingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

object VanillaCopies {
    /**
     * [FlyingEntity.travel]
     */
    fun travel(movementInput: Vec3d, entity: LivingEntity) {
        when {
            entity.isTouchingWater -> {
                entity.updateVelocity(0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(0.800000011920929)
            }
            entity.isInLava -> {
                entity.updateVelocity(0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(0.5)
            }
            else -> {
                val baseFrictionCoefficient = 0.91f
                val friction = if (entity.isOnGround) {
                    entity.world.getBlockState(BlockPos(entity.x, entity.y - 1.0, entity.z)).block
                        .slipperiness * baseFrictionCoefficient
                } else {
                    baseFrictionCoefficient
                }
                val g = 0.16277137f / (friction * friction * friction)

                entity.updateVelocity(if (entity.isOnGround) 0.1f * g else 0.02f, movementInput)
                entity.move(MovementType.SELF, entity.velocity)
                entity.velocity = entity.velocity.multiply(friction.toDouble())
            }
        }
        entity.method_29242(entity, false)
    }

    /**
     * Adapted from [MobEntity.lookAtEntity]
     */
    fun MobEntity.lookAtTarget(target: Vec3d, maxYawChange: Float, maxPitchChange: Float) {
        val d: Double = target.x - this.x
        val e: Double = target.z - this.z
        val g: Double = target.y - this.eyeY

        val h = MathHelper.sqrt(d * d + e * e).toDouble()
        val i = (MathHelper.atan2(e, d) * 57.2957763671875).toFloat() - 90.0f
        val j = (-(MathHelper.atan2(g, h) * 57.2957763671875)).toFloat()
        this.pitch = changeAngle(this.pitch, j, maxPitchChange)
        this.yaw = changeAngle(this.yaw, i, maxYawChange)
    }

    /**
     * [MobEntity.changeAngle]
     */
    private fun changeAngle(oldAngle: Float, newAngle: Float, maxChangeInAngle: Float): Float {
        var f = MathHelper.wrapDegrees(newAngle - oldAngle)
        if (f > maxChangeInAngle) {
            f = maxChangeInAngle
        }
        if (f < -maxChangeInAngle) {
            f = -maxChangeInAngle
        }
        return oldAngle + f
    }
}