package net.barribob.invasion.mob.ai

import net.barribob.invasion.utils.IVelPos
import net.minecraft.util.math.Vec3d

// https://gamedevelopment.tutsplus.com/tutorials/understanding-steering-behaviors-seek--gamedev-849
class VelocitySteering(private val velPos: IVelPos, private val maxVelocity: Double, mass: Double) :
    ISteering {

    private val inverseMass = if (mass == 0.0) throw IllegalArgumentException("Mass cannot be zero") else 1 / mass

    override fun accelerateTo(target: Vec3d): Vec3d = target
        .subtract(velPos.getPos())
        .normalize()
        .multiply(maxVelocity)
        .subtract(velPos.getVel())
        .multiply(inverseMass)
}