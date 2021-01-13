package net.barribob.boss.utils

import net.barribob.boss.mob.utils.animation.AnimationPredicate
import net.barribob.maelstrom.general.data.BooleanFlag
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

object AnimationUtils {
    fun <T : IAnimatable> createIdlePredicate(animationName: String): AnimationPredicate<T> = AnimationPredicate {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation(animationName, true)
        )
        PlayState.CONTINUE
    }

    fun checkAttackAnimation(
        it: AnimationEvent<*>,
        booleanFlag: BooleanFlag,
        animationName: String,
        idleAnimationName: String,
    ) {
        if (booleanFlag.getAndReset()) {
            it.controller.markNeedsReload()
            it.controller.setAnimation(
                AnimationBuilder()
                    .addAnimation(animationName, false)
                    .addAnimation(idleAnimationName, true)
            )
        }
    }
}