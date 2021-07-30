package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.projectile.SporeBallProjectile
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class SporeCodeAnimations : ICodeAnimations<SporeBallProjectile> {
    override fun animate(animatable: SporeBallProjectile, data: AnimationEvent<*>, geoModel: GeoModel<SporeBallProjectile>) {
        val pitch = if(animatable.impacted) animatable.pitch else MathHelper.lerpAngleDegrees(data.partialTick, animatable.pitch - 5, animatable.pitch)

        val model = geoModel.getModel(geoModel.getModelLocation(animatable))
        model.getBone("root1").ifPresent { it.rotationX = Math.toRadians(pitch.toDouble()).toFloat() }
    }
}