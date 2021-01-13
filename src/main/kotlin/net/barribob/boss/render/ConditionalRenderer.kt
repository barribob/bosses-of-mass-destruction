package net.barribob.boss.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import java.util.function.Predicate

class ConditionalRenderer<T : Entity>(private val predicate: Predicate<T>, private val renderer: IRenderer<T>) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        if (predicate.test(entity)) {
            renderer.render(entity, yaw, partialTicks, matrices, vertexConsumers, light)
        }
    }
}