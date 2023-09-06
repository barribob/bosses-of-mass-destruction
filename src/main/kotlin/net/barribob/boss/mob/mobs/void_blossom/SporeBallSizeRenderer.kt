package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.pow

class SporeBallSizeRenderer : IRenderer<SporeBallProjectile> {
    override fun render(
        entity: SporeBallProjectile,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val deathProgress: Float = if (!entity.impacted || entity.isRemoved) {
            0f
        } else {
            ((entity.impactedTicks + partialTicks) / SporeBallProjectile.explosionDelay) * 0.5f
        }
        val scaledDeathProgress = deathProgress.pow(2f) + 1
        matrices.scale(scaledDeathProgress, scaledDeathProgress, scaledDeathProgress)
    }
}