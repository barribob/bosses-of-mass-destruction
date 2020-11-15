package net.barribob.invasion.mob.utils.animation

import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class AnimationPredicate<T : IAnimatable>(val predicate: (AnimationEvent<*>) -> PlayState) :
    AnimationController.IAnimationPredicate<T> {
    override fun <P : IAnimatable> test(p0: AnimationEvent<P>): PlayState = predicate(p0)
}