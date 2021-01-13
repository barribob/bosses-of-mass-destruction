package net.barribob.boss.particle

import net.barribob.boss.Mod
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.registry.Registry

object Particles {
    val SKELETON: DefaultParticleType =
        Registry.register(Registry.PARTICLE_TYPE, Mod.identifier("skeleton"), FabricParticleTypes.simple())

    val DISAPPEARING_SWIRL: DefaultParticleType = Registry.register(Registry.PARTICLE_TYPE,
        Mod.identifier("disappearing_swirl"),
        FabricParticleTypes.simple())

    val SOUL_FLAME: DefaultParticleType = Registry.register(Registry.PARTICLE_TYPE,
        Mod.identifier("soul_flame"),
        FabricParticleTypes.simple())

    val LICH_MAGIC_CIRCLE: DefaultParticleType = Registry.register(Registry.PARTICLE_TYPE,
        Mod.identifier("magic_circle"),
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

        particleFactory.register(SOUL_FLAME) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it) { RandomUtils.range(15, 20) }
                particle.setColorOverride { ModColors.COMET_BLUE }
                particle
            }
        }

        particleFactory.register(LICH_MAGIC_CIRCLE) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                val particle = SimpleParticle(it) { 40 }
                particle.setBrightnessOverride { FULL_BRIGHT }
                particle.scale(4f)
                particle
            }
        }
    }
}