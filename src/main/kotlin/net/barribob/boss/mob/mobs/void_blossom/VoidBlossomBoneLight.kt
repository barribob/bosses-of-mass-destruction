package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.render.IBoneLight
import software.bernie.geckolib3.geo.render.built.GeoBone

class VoidBlossomBoneLight : IBoneLight {
    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        return if (bone.name.contains("Spike") || bone.name.contains("Thorn")) {
            IBoneLight.fullbright
        } else {
            packedLight
        }
    }
}