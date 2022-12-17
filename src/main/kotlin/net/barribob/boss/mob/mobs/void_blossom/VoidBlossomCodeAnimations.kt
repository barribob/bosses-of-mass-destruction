package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.GeoModel
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib.core.animation.AnimationState

class VoidBlossomCodeAnimations : ICodeAnimations<VoidBlossomEntity> {
    override fun animate(animatable: VoidBlossomEntity, data: AnimationState<*>, geoModel: GeoModel<VoidBlossomEntity>) {
        val bodyYaw = MathHelper.lerpAngleDegrees(data.partialTick, animatable.prevBodyYaw, animatable.bodyYaw)

        val model = geoModel.getBakedModel(geoModel.getModelResource(animatable))
        model.getBone("Leaves").ifPresent { it.rotY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
        model.getBone("Thorns").ifPresent { it.rotY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
        model.getBone("Roots").ifPresent { it.rotY = Math.toRadians(bodyYaw.toDouble()).toFloat() }
    }
}