package net.barribob.boss.mob.utils.animation

import net.barribob.boss.mob.GeoModel
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState

fun interface ICodeAnimations<T>  where T : GeoAnimatable {
    fun animate(animatable: T, data: AnimationState<*>, geoModel: GeoModel<T>)
}