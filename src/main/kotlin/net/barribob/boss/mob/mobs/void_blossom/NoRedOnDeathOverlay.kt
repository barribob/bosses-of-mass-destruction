package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.render.IOverlayOverride
import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

class NoRedOnDeathOverlay: IRenderer<VoidBlossomEntity>, IOverlayOverride {
    private var entity: VoidBlossomEntity? = null

    override fun render(
        entity: VoidBlossomEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        this.entity = entity
    }

    override fun getOverlay(): Int {
        val entity = entity ?: return OverlayTexture.packUv(OverlayTexture.getU(0f), OverlayTexture.getV(false))

        return OverlayTexture.packUv(OverlayTexture.getU(0f), OverlayTexture.getV(entity.hurtTime > 0 && !entity.isDead))
    }
}