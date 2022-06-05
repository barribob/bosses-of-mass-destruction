package net.barribob.boss.mob.utils.animation

import net.barribob.boss.mob.GeoModel
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.IAnimationTickable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

fun interface ICodeAnimations<T>  where T : IAnimatable, T: IAnimationTickable {
    fun animate(animatable: T, data: AnimationEvent<*>, geoModel: GeoModel<T>)
}