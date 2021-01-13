package net.barribob.boss.mob.ai

import net.barribob.boss.mob.utils.IEntity
import net.minecraft.util.math.Vec3d

// https://gamedevelopment.tutsplus.com/tutorials/understanding-steering-behaviors-seek--gamedev-849
class VelocitySteering(private val entity: IEntity, private val maxVelocity: Double, mass: Double) :
    ISteering {

    private val inverseMass = if (mass == 0.0) throw IllegalArgumentException("Mass cannot be zero") else 1 / mass

    override fun accelerateTo(target: Vec3d): Vec3d = target
        .subtract(entity.getPos())
        .normalize()
        .multiply(maxVelocity)
        .subtract(entity.getVel())
        .multiply(inverseMass)
}