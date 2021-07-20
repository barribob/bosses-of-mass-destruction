package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class VoidBlossomCodeAnimations : ICodeAnimations<VoidBlossomEntity> {
    override fun animate(animatable: VoidBlossomEntity, data: AnimationEvent<*>, geoModel: GeoModel<VoidBlossomEntity>) {
        val bodyYaw = MathHelper.lerpAngleDegrees(data.partialTick, animatable.prevBodyYaw, animatable.bodyYaw)

        val model = geoModel.getModel(geoModel.getModelLocation(animatable))
        model.getBone("Leaves").ifPresent { it.rotationY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
        model.getBone("Thorns").ifPresent { it.rotationY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
        model.getBone("Roots").ifPresent { it.rotationY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
    }
}