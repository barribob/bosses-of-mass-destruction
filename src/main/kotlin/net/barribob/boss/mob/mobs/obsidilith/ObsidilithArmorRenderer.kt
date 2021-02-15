package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.model.ObsidilithArmor
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import software.bernie.geckolib3.renderer.geo.GeoLayerRenderer
import software.bernie.geckolib3.renderer.geo.IGeoRenderer
import kotlin.random.Random

class ObsidilithArmorRenderer(entityRendererIn: IGeoRenderer<ObsidilithEntity>) : GeoLayerRenderer<ObsidilithEntity>(
    entityRendererIn
) {
    private val model = ObsidilithArmor()

    override fun render(
        stack: MatrixStack,
        buffer: VertexConsumerProvider,
        packedLight: Int,
        entity: ObsidilithEntity,
        limbSwing: Float,
        lastLimbDistance: Float,
        partialTicks: Float,
        animationProgress: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        if (entity.isShielded()) {
            val f: Float = entity.age.toFloat() + partialTicks
            val entityModel = model
            stack.scale(1.0f, -1.0f, 1.0f)
            stack.translate(0.0, -1.5010000467300415, 0.0)

            val vertexConsumer: VertexConsumer = buffer.getBuffer(
                RenderLayer.getEnergySwirl(
                    this.getEnergySwirlTexture(),
                    this.getEnergySwirlX(f),
                    f * 0.01f
                )
            )
            entityModel.render(stack, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 0.5f, 0.5f, 0.5f, 1.0f)
        }
    }

    private fun getEnergySwirlX(partialAge: Float): Float {
        return partialAge * Random.nextFloat()
    }

    private fun getEnergySwirlTexture(): Identifier {
        return Mod.identifier("textures/entity/obsidilith_armor.png")
    }
}