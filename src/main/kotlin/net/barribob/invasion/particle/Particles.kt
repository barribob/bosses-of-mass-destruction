package net.barribob.invasion.particle

import net.barribob.invasion.Invasions
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.registry.Registry

object Particles {
    val SKELETON: DefaultParticleType =
        Registry.register(Registry.PARTICLE_TYPE, Invasions.identifier("skeleton"), FabricParticleTypes.simple())

    val DISAPPEARING_SWIRL: DefaultParticleType = Registry.register(Registry.PARTICLE_TYPE,
        Invasions.identifier("disappearing_swirl"),
        FabricParticleTypes.simple())

    const val FULL_BRIGHT = 15728880

    fun clientInit() {
        val particleFactory = ParticleFactoryRegistry.getInstance()

        particleFactory
            .register(SKELETON) { provider: SpriteProvider ->
                SimpleParticleFactory(provider) {
                    SimpleParticle(it) {
                        RandomUtils.range(15, 20)
                    }
                }
            }

        particleFactory.register(DISAPPEARING_SWIRL) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it) {
                    RandomUtils.range(15, 20)
                }
            }
        }
    }
}