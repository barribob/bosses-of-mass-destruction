package net.barribob.boss.mob.utils

import net.barribob.boss.render.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ColorHelper
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
        isReRender: Boolean,
        partialTick: Float,
        packedLightIn: Int,
        packedOverlay: Int,
        colour: Int
    ) {
        val packedLight = iBoneLight?.getLightForBone(bone, packedLightIn) ?: packedLightIn
        val r = ColorHelper.Argb.getRed(colour)
        val g = ColorHelper.Argb.getGreen(colour)
        val b = ColorHelper.Argb.getBlue(colour)
        val a = ColorHelper.Argb.getAlpha(colour)
        val color = Vector4f(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat()).mul(1 / 255f)
        val newColor = (iBoneLight?.getColorForBone(bone, color) ?: color).mul(255f)
        super.renderRecursively(
            poseStack,
            animatable,
            bone,
            renderType,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            ColorHelper.Argb.getArgb(newColor.w.toInt(), newColor.x.toInt(), newColor.y.toInt(), newColor.z.toInt())
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
        colour: Int
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
            colour
        )
        val packetOverlay = overlayOverride?.getOverlay() ?: packedOverlay
        renderWithModel?.render(model, partialTick, poseStack, bufferSource, packedLight, packetOverlay, ColorHelper.Argb.getRed(colour).toFloat(), ColorHelper.Argb.getGreen(colour).toFloat(), ColorHelper.Argb.getBlue(colour).toFloat(), ColorHelper.Argb.getAlpha(colour).toFloat())
    }

    override fun getPackedOverlay(animatable: T, u: Float, partialTick: Float): Int {
        return overlayOverride?.getOverlay() ?: super.getPackedOverlay(animatable, u, partialTick)
    }

    override fun getDeathMaxRotation(entityLivingBaseIn: T): Float = if(deathRotation) 90f else 0f
}