package net.barribob.boss.projectile

import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f


class PetalBladeRenderer(
    private val dispatcher: EntityRenderDispatcher,
    private val renderLayer: RenderLayer,
) : IRenderer<PetalBladeProjectile> {
    override fun render(
        entity: PetalBladeProjectile,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val scale = 0.5f
        matrices.push()
        matrices.scale(scale, scale, scale)
        VanillaCopies.renderBillboard(matrices, vertexConsumers, light, dispatcher, renderLayer, Vec3f.POSITIVE_Z.getDegreesQuaternion(-entity.dataTracker.get(PetalBladeProjectile.renderRotation)))
        matrices.pop()
    }
}