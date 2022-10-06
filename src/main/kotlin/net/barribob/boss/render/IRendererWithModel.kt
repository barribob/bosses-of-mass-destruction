package net.barribob.boss.render

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import software.bernie.geckolib3.geo.render.built.GeoModel

interface IRendererWithModel {
    fun render(
        model: GeoModel,
        partialTicks: Float,
        type: RenderLayer,
        matrixStackIn: MatrixStack,
        renderTypeBuffer: VertexConsumerProvider?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    )
}