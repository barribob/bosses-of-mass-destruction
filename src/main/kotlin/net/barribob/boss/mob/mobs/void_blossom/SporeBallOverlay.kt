package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.pow

class SporeBallOverlay : IRenderer<SporeBallProjectile>, IOverlayOverride {
    private var entity: SporeBallProjectile? = null
    private var partialTicks: Float? = null

    override fun render(
        entity: SporeBallProjectile,
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
        val deathProgress: Float = if (!entity.impacted || entity.isRemoved) {
            0f
        } else {
            (entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay
        }
        val scaledDeathProgress = deathProgress.pow(2f)
        return OverlayTexture.packUv(OverlayTexture.getU(scaledDeathProgress), OverlayTexture.getV(false))
    }
}