package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.render.IRenderer
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

class LaserParticleRenderer: IRenderer<GauntletEntity> {
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
            val lerpedPos = MathUtils.lerpVec(partialTicks, beamPos.second, beamPos.first)
            val beamVel = MathUtils.unNormedDirection(beamPos.second, beamPos.first).normalize().multiply(0.1)
            val laserDir = MathUtils.unNormedDirection(entity.eyePos(), lerpedPos)
            entity.laserHandler.laserChargeParticles.build(entity.eyePos().add(laserDir.multiply(entity.random.nextDouble())), beamVel)
        }
    }
}