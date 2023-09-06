package net.barribob.boss.projectile

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

class PetalBladeParticleRenderer<T : Entity> : IRenderer<T> {
    private val petalParticleFactory = ClientParticleBuilder(Particles.PETAL)
        .color {
            if (it < 0.7) {
                MathUtils.lerpVec(it, ModColors.PINK, Vec3d(1.0, 0.85, 0.95))
            } else {
                MathUtils.lerpVec(it, Vec3d(1.0, 0.85, 0.95), ModColors.ULTRA_DARK_PURPLE)
            }
        }
        .brightness(Particles.FULL_BRIGHT)
        .colorVariation(0.15)
        .scale { RandomUtils.range(0.1, 0.2).toFloat() * (1 - it * 0.25f) }

    override fun render(
        entity: T,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val prevPos = Vec3d(entity.prevX, entity.prevY, entity.prevZ)
        val pos = MathUtils.lerpVec(partialTicks, prevPos, entity.pos)
        val dir = entity.pos.subtract(prevPos).normalize()

        val randomRot = RandomUtils.range(0, 360)
        val angularMomentum = RandomUtils.randSign() * 4f
        petalParticleFactory
            .continuousRotation { randomRot + it.getAge() * angularMomentum }
            .build(
                pos.add(RandomUtils.randVec().multiply(0.25)),
                RandomUtils.randVec().planeProject(VecUtils.yAxis)
                    .subtract(VecUtils.yAxis).normalize()
                    .multiply(0.1)
                    .add(dir.multiply(0.05))
            )
    }
}