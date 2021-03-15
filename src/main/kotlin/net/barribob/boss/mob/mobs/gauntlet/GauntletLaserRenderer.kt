package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

class GauntletLaserRenderer : IRenderer<GauntletEntity> {
    private val laserTexture = Mod.identifier("textures/entity/gauntlet_beam.png")
    private val layer: RenderLayer = RenderLayer.getEntityCutoutNoCull(laserTexture)

    override fun render(
        entity: GauntletEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        if (entity.laserHandler.shouldRenderLaser()) {
            val beamPos = entity.laserHandler.getLaserRenderPos()
            VanillaCopies.renderBeam(
                entity,
                beamPos.first,
                beamPos.second,
                partialTicks,
                ModColors.LASER_RED,
                matrices,
                vertexConsumers,
                layer
            )
        }
    }
}