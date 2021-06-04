package net.barribob.boss.render

import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector4f
import net.minecraft.util.Identifier
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.geo.render.built.GeoBone
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderer.geo.GeoBlockRenderer

class ModBlockEntityRenderer<T>(
    rendererDispatcherIn: BlockEntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>?,
    private val iBoneLight: IBoneLight? = null,
    ) : GeoBlockRenderer<T>(
    rendererDispatcherIn, modelProvider
) where T : BlockEntity, T : IAnimatable {
    override fun getRenderType(
        animatable: T,
        partialTicks: Float,
        stack: MatrixStack?,
        renderTypeBuffer: VertexConsumerProvider?,
        vertexBuilder: VertexConsumer?,
        packedLightIn: Int,
        textureLocation: Identifier?
    ): RenderLayer {
        return RenderLayer.getEntityCutoutNoCull(geoModelProvider.getTextureLocation(animatable))
    }

    override fun renderRecursively(
        bone: GeoBone,
        stack: MatrixStack?,
        bufferIn: VertexConsumer?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val packedLight = iBoneLight?.getLightForBone(bone, packedLightIn) ?: packedLightIn
        val color = Vector4f(red, green, blue, alpha)
        val newColor = iBoneLight?.getColorForBone(bone, color) ?: color
        super.renderRecursively(bone, stack, bufferIn, packedLight, packedOverlayIn, newColor.x, newColor.y, newColor.z, newColor.w)
    }
}