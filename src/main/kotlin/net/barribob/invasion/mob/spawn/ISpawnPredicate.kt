package net.barribob.invasion.mob.spawn

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

fun interface ISpawnPredicate {
    fun canSpawn(pos: Vec3d, entity: Entity): Boolean
}