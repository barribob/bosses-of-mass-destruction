package net.barribob.boss.mob.utils.animation

import net.barribob.boss.mob.GeoModel
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animation.AnimationState

fun interface ICodeAnimations<T>  where T : GeoAnimatable {
    fun animate(animatable: T, data: AnimationState<*>, geoModel: GeoModel<T>)
}