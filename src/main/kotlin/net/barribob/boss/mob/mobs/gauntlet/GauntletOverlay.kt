package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.sin

class GauntletOverlay : IRenderer<GauntletEntity>, IOverlayOverride {
    private var entity: GauntletEntity? = null
    private var partialTicks: Float? = null

    override fun render(
        entity: GauntletEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        this.entity = entity
        this.partialTicks = partialTicks
    }

    override fun getOverlay(): Int {
        val entity = entity ?: return OverlayTexture.packUv(OverlayTexture.getU(0f), OverlayTexture.getV(false))
        val partialTicks = partialTicks ?: 0f
        val deathProgress: Float = if (entity.deathTime == 0) {
            0f
        } else {
            (entity.deathTime + partialTicks) / ServerGauntletDeathHandler.deathAnimationTime
        }
        val flash = sin(deathProgress.toDouble() * 50).toFloat() * 0.1f + deathProgress * 0.9f
        return OverlayTexture.packUv(OverlayTexture.getU(flash), OverlayTexture.getV(entity.hurtTime > 0))
    }
}