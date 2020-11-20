package net.barribob.invasion

import net.barribob.invasion.animation.PauseAnimationTimer
import net.barribob.invasion.mob.Entities
import net.barribob.invasion.particle.SimpleParticle
import net.barribob.invasion.particle.SimpleParticleFactory
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.util.GlfwUtil
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import software.bernie.geckolib3.GeckoLib

object Invasions {
    const val MODID = "maelstrom_invasions"

    val LOGGER = ConsoleLogger(LogManager.getLogger())

    fun identifier(path: String) = Identifier(MODID, path)
}

object Particles {
    val SKELETON: DefaultParticleType =
        Registry.register(Registry.PARTICLE_TYPE, Invasions.identifier("skeleton"), FabricParticleTypes.simple())
}

@Suppress("unused")
fun init() {
    GeckoLib.initialize()

    Entities.init()
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    ParticleFactoryRegistry.getInstance()
        .register(Particles.SKELETON) { provider: SpriteProvider ->
            SimpleParticleFactory(provider) {
                SimpleParticle(it) {
                    RandomUtils.range(15, 20)
                }
            }
        }

    val animationTimer = PauseAnimationTimer({ GlfwUtil.getTime() * 20 }, { MinecraftClient.getInstance().isPaused })

    Entities.clientInit(animationTimer)
}