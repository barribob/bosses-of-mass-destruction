package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.render.IRendererWithModel
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import software.bernie.geckolib3.geo.render.built.GeoCube
import software.bernie.geckolib3.geo.render.built.GeoModel
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.model.provider.GeoModelProvider
import software.bernie.geckolib3.renderers.geo.IGeoRenderer
import kotlin.random.Random

class ObsidilithArmorRenderer(geoModel: AnimatedGeoModel<ObsidilithEntity>) : IRendererWithModel, IRenderer<ObsidilithEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")
    private val geoModelProvider = RenderHelper(geoModel)

    private var energyBuffer: VertexConsumer? = null
    private var entity: ObsidilithEntity? = null
    private var layer: RenderLayer? = null

    override fun render(
        model: GeoModel,
        partialTicks: Float,
        type: RenderLayer,
        matrixStackIn: MatrixStack,
        renderTypeBuffer: VertexConsumerProvider?,
        packedLightIn: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val buffer = energyBuffer ?: return
        val entity = entity ?: return
        val renderType = layer ?: return
        if (entity.isShielded()) {

            val color = when (entity.currentAttack) {
                ObsidilithUtils.burstAttackStatus -> ModColors.ORANGE
                ObsidilithUtils.waveAttackStatus -> ModColors.RED
                ObsidilithUtils.spikeAttackStatus -> ModColors.COMET_BLUE
                ObsidilithUtils.anvilAttackStatus -> ModColors.ENDER_PURPLE
                ObsidilithUtils.pillarDefenseStatus -> ModColors.WHITE
                else -> ModColors.WHITE
            }.add(VecUtils.unit).normalize().multiply(0.6)

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
                color.x.toFloat(), color.y.toFloat(), color.z.toFloat(), 1.0f
            )
        }
    }

    override fun render(
        entity: ObsidilithEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val renderAge: Float = entity.age + partialTicks
        val textureOffset = renderAge * Random.nextFloat()
        this.entity = entity
        layer = RenderLayer.getEnergySwirl(armorTexture, textureOffset, textureOffset)
        energyBuffer = vertexConsumers.getBuffer(layer)
    }

    private class RenderHelper(val geoModel: AnimatedGeoModel<ObsidilithEntity>) : IGeoRenderer<ObsidilithEntity> {
        override fun getGeoModelProvider(): GeoModelProvider<*> = geoModel
        override fun getTextureLocation(p0: ObsidilithEntity?): Identifier = Identifier("unused")

        private var provider: VertexConsumerProvider? = null

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
            matrixStack.scale(1.08f, 1.05f, 1.08f)
            super.renderCube(cube, matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha)
            matrixStack.pop()
        }

        override fun setCurrentRTB(rtb: VertexConsumerProvider?) {
            provider = rtb
        }

        override fun getCurrentRTB(): VertexConsumerProvider? {
            return provider
        }
    }
}