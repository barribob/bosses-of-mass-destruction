package net.barribob.boss.block

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.Vec3dReceiver
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.yOffset
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d

class BrimstoneNectarItemEffects : Vec3dReceiver {
    private val horizontalRodParticle = ClientParticleBuilder(Particles.HORIZONTAL_ROD)
        .color { MathUtils.lerpVec(it, ModColors.GOLD, ModColors.RUNIC_BROWN) }
        .colorVariation(0.25)
        .brightness(Particles.FULL_BRIGHT)
        .age(10, 15)

    private val particleBuilder2 = ClientParticleBuilder(Particles.EARTHDIVE_INDICATOR)
        .color { MathUtils.lerpVec(it, ModColors.RED, ModColors.DARK_RED) }
        .colorVariation(0.25)
        .brightness(Particles.FULL_BRIGHT)
        .age(30, 45)

    private val particleBuilder3 = ClientParticleBuilder(Particles.EARTHDIVE_INDICATOR)
        .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.GREY) }
        .colorVariation(0.25)
        .brightness(Particles.FULL_BRIGHT)
        .age(40, 50)

    override fun clientHandler(world: ClientWorld, vec3d: Vec3d) {
        for (i in 1..3) {
            spawnHorizontalRods(i.toDouble(), vec3d.yOffset(0.1))
        }

        for (i in 0..30) {
            val pos = vec3d.yOffset(RandomUtils.range(0.0, 1.5))
            val particleParams = ModUtils.RotatingParticles(pos, particleBuilder2, 1.5, 2.5, 0.0, 2.0)
            ModUtils.spawnRotatingParticles(particleParams)
        }

        for (i in 0..30) {
            val pos = vec3d.yOffset(RandomUtils.range(0.0, 1.5))
            val particleParams = ModUtils.RotatingParticles(pos, particleBuilder3, 2.0, 3.0, -1.0, 0.0)
            ModUtils.spawnRotatingParticles(particleParams)
        }
    }

    private fun spawnHorizontalRods(radius: Double, pos: Vec3d) {
        val numPoints = radius * 6
        MathUtils.circleCallback(radius, numPoints.toInt(), VecUtils.yAxis) {
            val offset = it.add(Vec3d(RandomUtils.range(-0.5, 0.5), 0.0, RandomUtils.range(-0.5, 0.5)))
            horizontalRodParticle
                .rotation(-MathUtils.directionToYaw(offset).toFloat() + 90)
                .build(pos.add(offset))
        }
    }
}