package net.barribob.boss.utils

import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.util.math.Vec3d

object ModColors {
    val WHITE = VecUtils.unit
    val COMET_BLUE = Vec3d(0.0, 1.0, 1.0)
    val FADED_COMET_BLUE = Vec3d(0.0, 0.3, 0.3)
    val TELEPORT_PURPLE = Vec3d(1.0, 0.5, 1.0)
    val ORANGE = Vec3d(1.0, 0.65, 0.1)
    val RUNIC_BROWN = Vec3d(0.66, 0.34, 0.0)
    val RED = Vec3d(0.8, 0.2, 0.4)
    val DARK_RED = Vec3d(0.4, 0.0, 0.0)
    val ENDER_PURPLE = Vec3d(158 / 255.0, 66 / 255.0, 245 / 255.0)
    val DARK_PURPLE: Vec3d = ENDER_PURPLE.multiply(0.75)
    val VOID_PURPLE = Vec3d(0.7, 0.3, 0.6)
    val LASER_RED = Vec3d(0.8, 0.1, 0.1)
    val GREY: Vec3d = VecUtils.unit.multiply(0.5)
    val GREEN = Vec3d(0.3, 0.8, 0.3)
    val DARK_GREEN = Vec3d(0.0, 0.5, 0.1)
    val PINK = Vec3d(0.9, 0.6, 0.8)
    val ULTRA_DARK_PURPLE = Vec3d(0.3, 0.0, 0.2)
    val DARK_GREY = Vec3d(0.3, 0.3, 0.3)
}
