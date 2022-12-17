package net.barribob.boss.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import software.bernie.geckolib.cache.`object`.BakedGeoModel

interface IRendererWithModel {
    fun render(
        model: BakedGeoModel,
        partialTicks: Float,
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