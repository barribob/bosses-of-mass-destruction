package net.barribob.invasion.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d

class ValidDirectionAnd(private val validators: List<IValidDirection>) : IValidDirection{
    override fun isValidDirection(normedDirection: Vec3d): Boolean = validators.all { it.isValidDirection(normedDirection) }
}