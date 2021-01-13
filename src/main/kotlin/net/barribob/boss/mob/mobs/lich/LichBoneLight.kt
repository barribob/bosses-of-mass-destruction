package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.render.IBoneLight
import net.minecraft.client.render.LightmapTextureManager
import software.bernie.geckolib3.geo.render.built.GeoBone

class LichBoneLight : IBoneLight {
    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        return if (bone.name in listOf("crown_crystals", "crystal")) {
            LightmapTextureManager.pack(15, 15)
        } else {
            return packedLight
        }
    }
}