package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.render.IBoneLight
import software.bernie.geckolib3.geo.render.built.GeoBone

class LichBoneLight : IBoneLight {
    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        return if (bone.name in listOf("crown_crystals", "crystal", "leftEye", "rightEye")) {
            15728880
        } else {
            packedLight
        }
    }
}