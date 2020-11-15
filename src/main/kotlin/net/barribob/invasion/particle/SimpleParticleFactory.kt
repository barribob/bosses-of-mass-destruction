package net.barribob.invasion.particle

import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.math.Vec3d

class SimpleParticleFactory(
    private val spriteProvider: SpriteProvider,
    private val particleFactory: (ParticleContext) -> Particle
) : ParticleFactory<DefaultParticleType> {
    override fun createParticle(
        parameters: DefaultParticleType?,
        world: ClientWorld,
        x: Double,
        y: Double,
        z: Double,
        velocityX: Double,
        velocityY: Double,
        velocityZ: Double
    ): Particle {
        return particleFactory(
            ParticleContext(
                spriteProvider,
                world,
                Vec3d(x, y, z),
                Vec3d(velocityX, velocityY, velocityZ)
            )
        )
    }
}