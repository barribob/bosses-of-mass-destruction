package net.barribob.boss.render

import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderer.geo.GeoBlockRenderer

class ModBlockEntityRenderer<T>(
    rendererDispatcherIn: BlockEntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>?
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
}