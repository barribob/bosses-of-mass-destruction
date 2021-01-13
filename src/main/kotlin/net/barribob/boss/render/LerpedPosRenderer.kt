package net.barribob.boss.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class LerpedPosRenderer <T : Entity> (val callback: (Vec3d) -> Unit) : IRenderer<T> {
    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val t = partialTicks.toDouble()
        val x = MathHelper.lerp(t, entity.prevX, entity.x)
        val y = MathHelper.lerp(t, entity.prevY, entity.y)
        val z = MathHelper.lerp(t, entity.prevZ, entity.z)

        callback(Vec3d(x, y, z))
    }
}