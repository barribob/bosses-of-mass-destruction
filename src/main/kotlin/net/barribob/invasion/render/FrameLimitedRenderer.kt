package net.barribob.invasion.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class FrameLimitedRenderer<T : Entity>(framesPerTick: Float, private val renderer: IRenderer<T>) : IRenderer<T> {
    private val limiter = FrameLimiter(framesPerTick)
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        if (limiter.canDoFrame(partialTicks)) {
            renderer.render(entity, yaw, partialTicks, matrices, vertexConsumers, light)
        }
    }
}