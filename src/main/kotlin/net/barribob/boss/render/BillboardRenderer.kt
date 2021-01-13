package net.barribob.boss.render

import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class BillboardRenderer<T : Entity>(
    private val dispatcher: EntityRenderDispatcher,
    private val renderLayer: RenderLayer,
    private val scale: () -> Float,
) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val scale = scale()
        matrices.push()
        matrices.scale(scale, scale, scale)
        VanillaCopies.renderBillboard(matrices, vertexConsumers, light, dispatcher, renderLayer)
        matrices.pop()
    }
}