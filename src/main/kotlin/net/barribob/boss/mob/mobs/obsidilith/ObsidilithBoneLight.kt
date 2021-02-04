package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.IRenderDataProvider
import net.barribob.boss.utils.ModColors
import net.minecraft.client.util.math.Vector4f
import software.bernie.geckolib3.geo.render.built.GeoBone

class ObsidilithBoneLight : IBoneLight, IRenderDataProvider<ObsidilithEntity> {
    var entity: ObsidilithEntity? = null
    var partialTicks: Float? = 0f
    private val defaultBoneColor = Vector4f(0.5f, 0.5f, 0.5f, 1f)

    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        return if (bone.name == "middle_runes" && entity?.currentAttack == ObsidilithUtils.burstAttackStatus) {
            IBoneLight.fullbright
        } else{
            packedLight
        }
    }

    override fun provide(entity: ObsidilithEntity, partialTicks: Float) {
        this.entity = entity
        this.partialTicks = partialTicks
    }

    override fun getColorForBone(bone: GeoBone, rgbaColor: Vector4f): Vector4f {
        if (bone.name == "middle_runes") {
            return if(entity?.currentAttack == ObsidilithUtils.burstAttackStatus) {
                Vector4f(
                    ModColors.ORANGE.x.toFloat(),
                    ModColors.ORANGE.y.toFloat(),
                    ModColors.ORANGE.z.toFloat(),
                    1.0f
                )
            } else {
                defaultBoneColor
            }
        }

        return super.getColorForBone(bone, rgbaColor)
    }
}