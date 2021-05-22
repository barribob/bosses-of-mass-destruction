package net.barribob.boss.mob

import net.barribob.boss.animation.IAnimationTimer
import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.render.ITextureProvider
import net.minecraft.util.Identifier
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.model.AnimatedGeoModel

class GeoModel<T : IAnimatable>(
    private val modelLocation: (T) -> Identifier,
    private val textureProvider: ITextureProvider<T>,
    private val animationLocation: Identifier,
    private val animationTimer: IAnimationTimer,
    private val codeAnimations: ICodeAnimations<T> = ICodeAnimations { _, _, _ -> }
) : AnimatedGeoModel<T>() {
    override fun getModelLocation(animatable: T): Identifier = modelLocation(animatable)
    override fun getTextureLocation(animatable: T): Identifier = textureProvider.getTexture(animatable)
    override fun getAnimationFileLocation(animatable: T): Identifier = animationLocation
    override fun getCurrentTick(): Double = animationTimer.getCurrentTick()

    override fun setLivingAnimations(entity: T?, uniqueID: Int?, customPredicate: AnimationEvent<*>?) {
        super.setLivingAnimations(entity, uniqueID, customPredicate)
        if (entity != null && customPredicate != null) codeAnimations.animate(entity, customPredicate, this)
    }
}