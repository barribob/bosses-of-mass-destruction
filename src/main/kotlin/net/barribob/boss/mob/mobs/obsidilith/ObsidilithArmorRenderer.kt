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
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import software.bernie.geckolib.cache.`object`.BakedGeoModel
import software.bernie.geckolib.cache.`object`.GeoCube
import software.bernie.geckolib.model.GeoModel
import software.bernie.geckolib.renderer.GeoEntityRenderer
import kotlin.random.Random

class ObsidilithArmorRenderer(val geoModel: GeoModel<ObsidilithEntity>, val context: EntityRendererFactory.Context) : IRendererWithModel, IRenderer<ObsidilithEntity> {
    private val armorTexture = Mod.identifier("textures/entity/obsidilith_armor.png")
    private var geoModelProvider: RenderHelper? = null 

    private var energyBuffer: VertexConsumer? = null
    private var entity: ObsidilithEntity? = null
    private var layer: RenderLayer? = null

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
        if (geoModelProvider == null) 
            geoModelProvider = RenderHelper(entity, geoModel, context)
        this.entity = entity
        layer = RenderLayer.getEnergySwirl(armorTexture, textureOffset, textureOffset)
    }

    private class RenderHelper(val entity: ObsidilithEntity, parentModel: GeoModel<ObsidilithEntity>, context: EntityRendererFactory.Context) : GeoEntityRenderer<ObsidilithEntity>(context, parentModel) {
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
            matrixStack.scale(1.08f, 1.05f, 1.08f)
            super.renderCube(matrixStack, cube, buffer, packedLight, packedOverlay, red, green, blue, alpha)
            matrixStack.pop()
        }
        
        override fun getAnimatable(): ObsidilithEntity = entity
    }
}