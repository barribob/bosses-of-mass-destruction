package net.barribob.boss.mob

import net.barribob.boss.mob.utils.animation.ICodeAnimations
import net.barribob.boss.render.ITextureProvider
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Identifier
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.model.GeoModel

class GeoModel<T>(
    private val modelLocation: (T) -> Identifier,
    private val textureProvider: ITextureProvider<T>,
    private val animationLocation: Identifier,
    private val codeAnimations: ICodeAnimations<T> = ICodeAnimations { _, _, _ -> },
    private val renderLayer: (Identifier) -> RenderLayer = { RenderLayer.getEntityCutout(it) } 
) : GeoModel<T>() where T : GeoAnimatable {
    override fun getModelResource(animatable: T): Identifier = modelLocation(animatable)
    override fun getTextureResource(animatable: T): Identifier = textureProvider.getTexture(animatable)
    override fun getAnimationResource(animatable: T): Identifier = animationLocation
    override fun getRenderType(animatable: T, texture: Identifier): RenderLayer = renderLayer(texture)

    override fun setCustomAnimations(entity: T, instanceId: Long, customPredicate: AnimationState<T>?) {
        if (customPredicate != null) codeAnimations.animate(entity, customPredicate, this)
    }
}