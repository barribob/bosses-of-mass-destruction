package net.barribob.boss.mob.utils

import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.IRenderLight
import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.geo.render.built.GeoBone
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRenderDispatcher?,
    modelProvider: AnimatedGeoModel<T>?,
    private val brightness: IRenderLight<T>? = null,
    private val brightnessByCube: IBoneLight? = null,
    private val renderer: IRenderer<T>? = null,
    ) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : IAnimatable, T : LivingEntity {

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
    }

    override fun renderRecursively(
        bone: GeoBone,
        stack: MatrixStack?,
        bufferIn: VertexConsumer?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val packedLight = brightnessByCube?.getLightForBone(bone, packedLightIn) ?: packedLightIn
        super.renderRecursively(bone, stack, bufferIn, packedLight, packedOverlayIn, red, green, blue, alpha)
    }

    override fun render(
        entity: T,
        yaw: Float,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        renderer?.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.push()
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.pop()
    }
}