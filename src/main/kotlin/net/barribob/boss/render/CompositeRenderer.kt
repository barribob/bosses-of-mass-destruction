package net.barribob.boss.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class CompositeRenderer<T : Entity>(vararg renderers: IRenderer<T>) : IRenderer<T> {
    var rendererList = renderers.toList()

    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        rendererList.forEach { it.render(entity, yaw, partialTicks, matrices, vertexConsumers, light) }
    }
}