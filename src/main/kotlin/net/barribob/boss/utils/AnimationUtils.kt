package net.barribob.boss.utils

import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.PlayState
import software.bernie.geckolib.animation.RawAnimation

object AnimationUtils {
    fun <T : GeoAnimatable> createIdlePredicate(animationName: String): AnimationController.AnimationStateHandler<T> = AnimationController.AnimationStateHandler {
        it.controller.setAnimation(
            RawAnimation.begin().thenLoop(animationName)
        )
        PlayState.CONTINUE
    }
}