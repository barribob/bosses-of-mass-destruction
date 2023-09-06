package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.projectile.SporeBallProjectile
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib.core.animation.AnimationState

class SporeCodeAnimations : ICodeAnimations<SporeBallProjectile> {
    override fun animate(animatable: SporeBallProjectile, data: AnimationState<*>, geoModel: GeoModel<SporeBallProjectile>) {
        val pitch = if(animatable.impacted) animatable.pitch else MathHelper.lerpAngleDegrees(data.partialTick, animatable.pitch - 5, animatable.pitch)

        val model = geoModel.getBakedModel(geoModel.getModelResource(animatable))
        model.getBone("root1").ifPresent { it.rotX = Math.toRadians(pitch.toDouble()).toFloat() }
    }
}