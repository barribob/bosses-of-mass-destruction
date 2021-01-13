package net.barribob.boss.render

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class SimpleEntityRenderer<T : Entity>(
    renderManager: EntityRenderDispatcher,
    private val renderer: IRenderer<T>,
    private val textureProvider: ITextureProvider<T>,
    private val brightness: IRenderLight<T>? = null,
) : EntityRenderer<T>(renderManager) {
    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        renderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }

    override fun getTexture(entity: T): Identifier = textureProvider.getTexture(entity)

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int =
        brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
}