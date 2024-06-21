package net.barribob.boss.projectile.comet

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib.animation.AnimationState

class CometCodeAnimations : ICodeAnimations<CometProjectile> {
    override fun animate(
        animatable: CometProjectile,
        data: AnimationState<*>,
        geoModel: GeoModel<CometProjectile>,
    ) {
        val pitch = MathHelper.lerpAngleDegrees(data.partialTick, animatable.pitch - 5, animatable.pitch)

        val model = geoModel.getBakedModel(geoModel.getModelResource(animatable))

        model.getBone("root1").ifPresent { it.rotX = Math.toRadians(pitch.toDouble()).toFloat() }
    }
}