package net.barribob.invasion.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class CompositeRenderer<T : Entity>(private val renderers: List<IRenderer<T>>) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        renderers.forEach { it.render(entity, yaw, partialTicks, matrices, vertexConsumers, light) }
    }
}