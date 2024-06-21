package net.barribob.boss.render

import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.joml.Vector4f
import software.bernie.geckolib.animatable.GeoAnimatable
import software.bernie.geckolib.cache.`object`.GeoBone
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoBlockRenderer

class ModBlockEntityRenderer<T>(
    modelProvider: GeoModel<T>?,
    private val iBoneLight: IBoneLight? = null,
) : GeoBlockRenderer<T>(
    modelProvider
) where T : BlockEntity, T : GeoAnimatable {
    override fun getRenderType(animatable: T, texture: Identifier?, bufferSource: VertexConsumerProvider?, partialTick: Float): RenderLayer {
        return RenderLayer.getEntityCutoutNoCull(model.getTextureResource(animatable))
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
        packedLight: Int,
        packedOverlay: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val packedLight = iBoneLight?.getLightForBone(bone, packedLight) ?: packedLight
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
            newColor.x, newColor.y, newColor.z, newColor.w
        )
    }
}