package net.barribob.boss.render

import net.minecraft.client.util.math.Vector4f
import software.bernie.geckolib3.geo.render.built.GeoBone

interface IBoneLight {
    fun getLightForBone(bone: GeoBone, packedLight: Int): Int
    fun getColorForBone(bone: GeoBone, rgbaColor: Vector4f): Vector4f = rgbaColor

    companion object {
        const val fullbright: Int = 15728880
    }
}