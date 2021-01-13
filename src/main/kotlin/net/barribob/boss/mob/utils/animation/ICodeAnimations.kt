package net.barribob.boss.mob.utils.animation

import net.barribob.boss.mob.GeoModel
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

fun interface ICodeAnimations<T>  where T : IAnimatable  {
    fun animate(animatable: T, data: AnimationEvent<*>, geoModel: GeoModel<T>)
}