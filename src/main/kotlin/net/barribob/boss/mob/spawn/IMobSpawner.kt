package net.barribob.boss.mob.spawn

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

fun interface IMobSpawner {
    fun spawn(pos: Vec3d, entity: Entity)
}