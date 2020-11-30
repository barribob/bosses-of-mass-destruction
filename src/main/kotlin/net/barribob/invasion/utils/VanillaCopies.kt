package net.barribob.invasion.utils

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.mob.FlyingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.projectile.thrown.SnowballEntity
import net.minecraft.sound.SoundEvents
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

    // Todo: Temporary placeholder projectile - do we want to use this, or bring our custom projectiles from the core?
    fun attack(actor: PathAwareEntity, target: LivingEntity) {
        val snowballEntity = SnowballEntity(actor.world, actor)
        val d = target.eyeY - 1.100000023841858
        val e: Double = target.x - actor.getX()
        val f: Double = d - snowballEntity.y
        val g: Double = target.z - actor.getZ()
        val h = MathHelper.sqrt(e * e + g * g) * 0.2f
        snowballEntity.setVelocity(e, f + h.toDouble(), g, 1.6f, 12.0f)
        actor.playSound(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 1.0f, 0.4f / (actor.getRandom().nextFloat() * 0.4f + 0.8f))
        actor.world.spawnEntity(snowballEntity)
    }
}