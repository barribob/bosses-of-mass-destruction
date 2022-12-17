package net.barribob.boss.render

import org.joml.Vector4f
import software.bernie.geckolib.cache.`object`.GeoBone

fun interface IBoneLight {
    fun getLightForBone(bone: GeoBone, packedLight: Int): Int
    fun getColorForBone(bone: GeoBone, rgbaColor: Vector4f): Vector4f = rgbaColor

    companion object {
        const val fullbright: Int = 15728880
    }
}