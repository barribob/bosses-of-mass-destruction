package net.barribob.invasion.render

import software.bernie.geckolib3.geo.render.built.GeoBone

interface IBoneLight {
    fun getLightForBone(bone: GeoBone, packedLight: Int): Int
}