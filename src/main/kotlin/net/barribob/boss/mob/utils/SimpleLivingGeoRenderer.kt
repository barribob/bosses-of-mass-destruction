package net.barribob.boss.mob.utils

import net.barribob.boss.render.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import org.joml.Vector4f
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer

class SimpleLivingGeoRenderer<T>(
    renderManager: EntityRendererFactory.Context?,
    modelProvider: GeoModel<T>,
    private val brightness: IRenderLight<T>? = null,
    private val iBoneLight: IBoneLight? = null,
    private val renderer: IRenderer<T>? = null,
    private val renderWithModel: IRendererWithModel? = null,
    private val overlayOverride: IOverlayOverride? = null,
    private val deathRotation: Boolean = true
    ) : GeoEntityRenderer<T>(renderManager, modelProvider) where T : GeoAnimatable, T : Entity {

    override fun getBlockLight(entity: T, blockPos: BlockPos): Int {
        return brightness?.getBlockLight(entity, blockPos) ?: super.getBlockLight(entity, blockPos)
    }

    override fun renderRecursively(
        poseStack: MatrixStack?,
        animatable: T,
        bone: GeoBone,
        renderType: RenderLayer?,
        bufferSource: VertexConsumerProvider?,
        buffer: VertexConsumer?,
        skipGeoLayers: Boolean,
        partialTick: Float,
        packedLightIn: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val packedLight = iBoneLight?.getLightForBone(bone, packedLightIn) ?: packedLightIn
        val color = Vector4f(red, green, blue, alpha)
        val newColor = iBoneLight?.getColorForBone(bone, color) ?: color
        super.renderRecursively(
            poseStack,
            animatable,
            bone,
            renderType,
            bufferSource,
            buffer,
            skipGeoLayers,
            partialTick,
            packedLight,
            packedOverlay,
            newColor.x,
            newColor.y,
            newColor.z,
            newColor.w
        )
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

    override fun actuallyRender(
        poseStack: MatrixStack,
        animatable: T,
        model: BakedGeoModel,
        renderType: RenderLayer?,
        bufferSource: VertexConsumerProvider?,
        buffer: VertexConsumer?,
        isReRender: Boolean,
        partialTick: Float,
        packedLight: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        super.actuallyRender(
            poseStack,
            animatable,
            model,
            renderType,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            red,
            green,
            blue,
            alpha
        )
        val packetOverlay = overlayOverride?.getOverlay() ?: packedOverlay
        renderWithModel?.render(model, partialTick, poseStack, bufferSource, packedLight, packetOverlay, red, green, blue, alpha)
    }

    override fun getPackedOverlay(animatable: T, u: Float, partialTick: Float): Int {
        return overlayOverride?.getOverlay() ?: super.getPackedOverlay(animatable, u, partialTick)
    }

    override fun getDeathMaxRotation(entityLivingBaseIn: T): Float = if(deathRotation) 90f else 0f
}