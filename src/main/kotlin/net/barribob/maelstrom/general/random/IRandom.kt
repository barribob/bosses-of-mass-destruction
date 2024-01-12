package net.barribob.maelstrom.general.random

import net.minecraft.util.math.Vec3d

interface IRandom {
    fun getDouble() : Double
    fun getVector() : Vec3d
}