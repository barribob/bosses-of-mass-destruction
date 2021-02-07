package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.IRenderDataProvider
import net.barribob.boss.utils.ModColors
import net.minecraft.client.util.math.Vector4f
import net.minecraft.util.math.Vec3d
import software.bernie.geckolib3.geo.render.built.GeoBone

class ObsidilithBoneLight : IBoneLight, IRenderDataProvider<ObsidilithEntity> {
    var entity: ObsidilithEntity? = null
    var partialTicks: Float? = 0f
    private val defaultBoneColor = Vector4f(0.5f, 0.5f, 0.5f, 1f)

    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        val entity1 = entity
        return if (bone.name == "middle_runes" && entity1 != null && entity1.currentAttack > 0) {
            IBoneLight.fullbright
        } else {
            packedLight
        }
    }

    override fun provide(entity: ObsidilithEntity, partialTicks: Float) {
        this.entity = entity
        this.partialTicks = partialTicks
    }

    override fun getColorForBone(bone: GeoBone, rgbaColor: Vector4f): Vector4f {
        if (bone.name == "middle_runes") {
            return when (entity?.currentAttack) {
                ObsidilithUtils.burstAttackStatus -> colorToVec4(ModColors.ORANGE)
                ObsidilithUtils.waveAttackStatus -> colorToVec4(ModColors.RED)
                ObsidilithUtils.spikeAttackStatus -> colorToVec4(ModColors.COMET_BLUE)
                ObsidilithUtils.anvilAttackStatus -> colorToVec4(ModColors.ENDER_PURPLE)
                else -> defaultBoneColor
            }
        }

        return super.getColorForBone(bone, rgbaColor)
    }

    private fun colorToVec4(color: Vec3d): Vector4f = Vector4f(
        color.x.toFloat(),
        color.y.toFloat(),
        color.z.toFloat(),
        1.0f
    )
}