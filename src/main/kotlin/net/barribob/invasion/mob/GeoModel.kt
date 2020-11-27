package net.barribob.invasion.mob

import net.barribob.invasion.animation.IAnimationTimer
import net.barribob.invasion.mob.utils.animation.ICodeAnimations
import net.minecraft.util.Identifier
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.model.AnimatedGeoModel

class GeoModel<T : IAnimatable>(
    private val modelLocation: Identifier,
    private val textureLocation: Identifier,
    private val animationLocation: Identifier,
    private val animationTimer: IAnimationTimer,
    private val codeAnimations: ICodeAnimations<T> = ICodeAnimations { _, _, _ -> }
) : AnimatedGeoModel<T>() {
    override fun getModelLocation(animatable: T): Identifier = modelLocation
    override fun getTextureLocation(animatable: T): Identifier = textureLocation
    override fun getAnimationFileLocation(animatable: T): Identifier = animationLocation
    override fun getCurrentTick(): Float = animationTimer.getCurrentTick()

    override fun setLivingAnimations(entity: T, uniqueID: Int?, customPredicate: AnimationEvent<*>) {
        super.setLivingAnimations(entity, uniqueID, customPredicate)
        codeAnimations.animate(entity, customPredicate, this)
    }
}