package net.barribob.invasion.projectile.comet

import net.barribob.invasion.mob.GeoModel
import net.barribob.invasion.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class CometCodeAnimations : ICodeAnimations<CometProjectile> {
    override fun animate(
        animatable: CometProjectile,
        data: AnimationEvent<*>,
        geoModel: GeoModel<CometProjectile>,
    ) {
        val pitch = MathHelper.lerpAngleDegrees(data.partialTick, animatable.prevPitch, animatable.pitch)

        val model = geoModel.getModel(geoModel.getModelLocation(animatable))

        model.getBone("root1").ifPresent { it.rotationX = Math.toRadians(pitch.toDouble()).toFloat() }
    }
}