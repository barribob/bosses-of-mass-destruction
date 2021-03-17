package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.IRendererWithModel
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import software.bernie.geckolib3.geo.render.built.GeoCube
import software.bernie.geckolib3.geo.render.built.GeoModel
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.model.provider.GeoModelProvider
import software.bernie.geckolib3.renderer.geo.IGeoRenderer

class GauntletEnergyRenderer(val geoModel: AnimatedGeoModel<GauntletEntity>) : IRendererWithModel,
    IRenderer<GauntletEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")
    private val geoModelProvider = RenderHelper(geoModel)

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
        gauntletEntity = entity
        layer = RenderLayer.getEnergySwirl(armorTexture, textureOffset, textureOffset)
        energyBuffer = vertexConsumers.getBuffer(layer)
    }

    override fun render(
        model: GeoModel,
        partialTicks: Float,
        type: RenderLayer,
        matrixStackIn: MatrixStack,
        renderTypeBuffer: VertexConsumerProvider,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val buffer = energyBuffer ?: return
        val entity = gauntletEntity ?: return
        val renderType = layer ?: return
        val renderAlpha = entity.energyShieldHandler.getRenderAlpha()
        if(renderAlpha == 0f) return
        val lerpedAlpha = MathHelper.lerp(partialTicks, renderAlpha - 0.1f, renderAlpha)
        geoModelProvider.render(
            model,
            entity,
            partialTicks,
            renderType,
            matrixStackIn,
            renderTypeBuffer,
            buffer,
            packedLightIn,
            OverlayTexture.DEFAULT_UV,
            0.8f * lerpedAlpha,
            0.2f * lerpedAlpha,
            0.2f * lerpedAlpha,
            lerpedAlpha
        )
    }

    private class RenderHelper(val geoModel: AnimatedGeoModel<GauntletEntity>) : IGeoRenderer<GauntletEntity> {
        override fun getGeoModelProvider(): GeoModelProvider<*> = geoModel
        override fun getTextureLocation(p0: GauntletEntity?): Identifier = Identifier("unused")

        override fun renderCube(
            cube: GeoCube,
            matrixStack: MatrixStack,
            bufferIn: VertexConsumer,
            packedLightIn: Int,
            packedOverlayIn: Int,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float
        ) {
            matrixStack.push()
            matrixStack.scale(1.1f, 1.05f, 1.1f)
            super.renderCube(cube, matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha)
            matrixStack.pop()
        }
    }
}