package net.barribob.boss

import net.barribob.boss.animation.PauseAnimationTimer
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.sound.ModSounds
import net.barribob.boss.utils.InGameTests
import net.barribob.boss.utils.NetworkUtils
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.GlfwUtil
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import software.bernie.geckolib3.GeckoLib

object Mod {
    const val MODID = "bosses_of_mass_destruction"

    val LOGGER = ConsoleLogger(LogManager.getLogger())

    val sounds: ModSounds = ModSounds()

    fun identifier(path: String) = Identifier(MODID, path)
}

@Suppress("unused")
fun init() {
    MaelstromMod.testCommand.addId(InGameTests::throwProjectile.name, InGameTests::throwProjectile)
    MaelstromMod.testCommand.addId(InGameTests::axisOffset.name, InGameTests::axisOffset)
    MaelstromMod.testCommand.addId(InGameTests::spawnEntity.name, InGameTests::spawnEntity)
    MaelstromMod.testCommand.addId(InGameTests::testClient.name, InGameTests::testClient)

    GeckoLib.initialize()

    Entities.init()

    Mod.sounds.init()
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    val animationTimer = PauseAnimationTimer({ GlfwUtil.getTime() * 20 }, { MinecraftClient.getInstance().isPaused })

    ClientSidePacketRegistry.INSTANCE.register(NetworkUtils.SPAWN_ENTITY_PACKET_ID, NetworkUtils::handleSpawnClientEntity)
    ClientSidePacketRegistry.INSTANCE.register(NetworkUtils.CLIENT_TEST_PACKET_ID, NetworkUtils::handleTestClient)

    Entities.clientInit(animationTimer)
    Particles.clientInit()
}