package net.barribob.boss.utils

import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.RawAnimation

object AnimationUtils {
    fun <T : GeoAnimatable> createIdlePredicate(animationName: String): AnimationController.AnimationStateHandler<T> = AnimationController.AnimationStateHandler {
        it.controller.setAnimation(
            RawAnimation.begin().thenLoop(animationName)
        )
        PlayState.CONTINUE
    }
}