package net.barribob.invasion.mob.ai

import net.minecraft.util.math.Vec3d

interface ITargetSelector {
    fun getTarget(): Vec3d
}