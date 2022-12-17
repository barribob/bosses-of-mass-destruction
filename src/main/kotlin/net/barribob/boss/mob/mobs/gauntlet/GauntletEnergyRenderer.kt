package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.IRendererWithModel
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.`object`.GeoCube
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer

class GauntletEnergyRenderer(val geoModel: GeoModel<GauntletEntity>,  val context: EntityRendererFactory.Context) : IRendererWithModel,
    IRenderer<GauntletEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")
    private var geoModelProvider: RenderHelper? = null 

    private var energyBuffer: VertexConsumer? = null
    private var gauntletEntity: GauntletEntity? = null
    private var layer: RenderLayer? = null

    override fun render(
        entity: GauntletEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val renderAge: Float = entity.age + partialTicks
        val textureOffset = renderAge * 0.01f
        if (geoModelProvider == null)
            geoModelProvider = RenderHelper(entity, geoModel, context)
        gauntletEntity = entity
        layer = RenderLayer.getEnergySwirl(armorTexture, textureOffset, textureOffset)
    }

    override fun render(
        model: BakedGeoModel,
        partialTicks: Float,
        matrixStackIn: MatrixStack,
        renderTypeBuffer: VertexConsumerProvider?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        energyBuffer = renderTypeBuffer?.getBuffer(layer)
        val buffer = energyBuffer ?: return
        val entity = gauntletEntity ?: return
        val renderType = layer ?: return
        val renderAlpha = entity.energyShieldHandler.getRenderAlpha()
        if(renderAlpha == 0f) return
        val lerpedAlpha = MathHelper.lerp(partialTicks, renderAlpha - 0.1f, renderAlpha)
        geoModelProvider?.actuallyRender(
            matrixStackIn,
            entity,
            model,
            renderType,
            renderTypeBuffer,
            buffer,
            false,
            partialTicks,
            packedLightIn,
            OverlayTexture.DEFAULT_UV,
            0.8f * lerpedAlpha,
            0.2f * lerpedAlpha,
            0.2f * lerpedAlpha,
            lerpedAlpha
        )
    }

    private class RenderHelper(val gauntletEntity: GauntletEntity, parentModel: GeoModel<GauntletEntity>, context: EntityRendererFactory.Context) : GeoEntityRenderer<GauntletEntity>(context, parentModel) {
        override fun renderCube(
            matrixStack: MatrixStack,
            cube: GeoCube?,
            buffer: VertexConsumer?,
            packedLight: Int,
            packedOverlay: Int,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float
        ) {
            matrixStack.push()
            matrixStack.scale(1.1f, 1.05f, 1.1f)
            super.renderCube(matrixStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha)
            matrixStack.pop()
        }

    override fun getAnimatable(): GauntletEntity = gauntletEntity
    }
}