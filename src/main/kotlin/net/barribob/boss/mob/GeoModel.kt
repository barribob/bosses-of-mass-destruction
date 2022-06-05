package net.barribob.boss.mob

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.render.ITextureProvider
import net.minecraft.util.Identifier
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.IAnimationTickable
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.model.AnimatedTickingGeoModel

class GeoModel<T>(
    private val modelLocation: (T) -> Identifier,
    private val textureProvider: ITextureProvider<T>,
    private val animationLocation: Identifier,
    private val codeAnimations: ICodeAnimations<T> = ICodeAnimations { _, _, _ -> }
) : AnimatedTickingGeoModel<T>() where T : IAnimatable, T : IAnimationTickable {
    override fun getModelResource(animatable: T): Identifier = modelLocation(animatable)
    override fun getTextureResource(animatable: T): Identifier = textureProvider.getTexture(animatable)
    override fun getAnimationResource(animatable: T): Identifier = animationLocation

    override fun setLivingAnimations(entity: T?, uniqueID: Int?, customPredicate: AnimationEvent<*>?) {
        super.setLivingAnimations(entity, uniqueID, customPredicate)
        if (entity != null && customPredicate != null) codeAnimations.animate(entity, customPredicate, this)
    }
}