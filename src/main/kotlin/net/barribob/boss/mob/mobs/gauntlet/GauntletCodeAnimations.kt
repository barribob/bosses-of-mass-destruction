package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class GauntletCodeAnimations: ICodeAnimations<GauntletEntity> {
    override fun animate(animatable: GauntletEntity, data: AnimationEvent<*>, geoModel: GeoModel<GauntletEntity>) {
        val headPitch = MathHelper.lerp(data.partialTick, animatable.prevPitch, animatable.pitch)
        val model = geoModel.getModel(geoModel.getModelResource(animatable))

        model.getBone("codeRoot").ifPresent { it.rotationX = -Math.toRadians(headPitch.toDouble()).toFloat() }
    }
}