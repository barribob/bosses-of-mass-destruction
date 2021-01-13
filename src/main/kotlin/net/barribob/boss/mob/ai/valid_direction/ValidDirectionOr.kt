package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d

class ValidDirectionOr(private val validators: List<IValidDirection>) : IValidDirection{
    override fun isValidDirection(normedDirection: Vec3d): Boolean = validators.any { it.isValidDirection(normedDirection) }
}