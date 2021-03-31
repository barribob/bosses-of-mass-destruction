package net.barribob.boss.mob.utils

import net.minecraft.entity.MovementType
import net.minecraft.util.math.Vec3d

interface IMoveHandler {
    fun canMove(type: MovementType, movement: Vec3d): Boolean
}