package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib.core.animation.AnimationState

class GauntletCodeAnimations: ICodeAnimations<GauntletEntity> {
    override fun animate(animatable: GauntletEntity, data: AnimationState<*>, geoModel: GeoModel<GauntletEntity>) {
        val headPitch = MathHelper.lerp(data.partialTick, animatable.prevPitch, animatable.pitch)
        val model = geoModel.getBakedModel(geoModel.getModelResource(animatable))

        model.getBone("codeRoot").ifPresent { it.rotX = -Math.toRadians(headPitch.toDouble()).toFloat() }
    }
}