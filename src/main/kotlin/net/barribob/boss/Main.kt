package net.barribob.boss

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer
import net.barribob.boss.Mod.networkUtils
import net.barribob.boss.animation.PauseAnimationTimer
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.config.ModConfig
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.sound.ModSounds
import net.barribob.boss.utils.InGameTests
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.NetworkUtils
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.GlfwUtil
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import software.bernie.geckolib3.GeckoLib

object Mod {
    const val MODID = "bosses_of_mass_destruction"

    val LOGGER = ConsoleLogger(LogManager.getLogger())

    val sounds: ModSounds = ModSounds()

    val networkUtils = NetworkUtils(MaelstromMod.isDevelopmentEnvironment)

    fun identifier(path: String) = Identifier(MODID, path)
}

@Suppress("unused")
fun init() {
    AutoConfig.register(ModConfig::class.java, ::JanksonConfigSerializer)

    GeckoLib.initialize()

    ModBlocks.init()
    Entities.init()
    ModStructures.init()

    Mod.sounds.init()

    if(MaelstromMod.isDevelopmentEnvironment) initDev()
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    val animationTimer = PauseAnimationTimer({ GlfwUtil.getTime() * 20 }, { MinecraftClient.getInstance().isPaused })

    networkUtils.registerClientHandlers()

    Entities.clientInit(animationTimer)
    Particles.clientInit()
}

private fun initDev() {
    val inGameTests = InGameTests(MaelstromMod.debugPoints, networkUtils)
    MaelstromMod.testCommand.addId(inGameTests::throwProjectile.name, inGameTests::throwProjectile)
    MaelstromMod.testCommand.addId(inGameTests::axisOffset.name, inGameTests::axisOffset)
    MaelstromMod.testCommand.addId(inGameTests::spawnEntity.name, inGameTests::spawnEntity)
    MaelstromMod.testCommand.addId(inGameTests::testClient.name, inGameTests::testClient)
    MaelstromMod.testCommand.addId(inGameTests::lichSummon.name, inGameTests::lichSummon)
    MaelstromMod.testCommand.addId(inGameTests::lichCounter.name, inGameTests::lichCounter)
    MaelstromMod.testCommand.addId(inGameTests::burstAction.name, inGameTests::burstAction)
    MaelstromMod.testCommand.addId(inGameTests::playerPosition.name, inGameTests::playerPosition)
    MaelstromMod.testCommand.addId(inGameTests::placePillars.name, inGameTests::placePillars)
    MaelstromMod.testCommand.addId(inGameTests::placeObsidian.name, inGameTests::placeObsidian)
    MaelstromMod.testCommand.addId(inGameTests::obsidilithDeath.name, inGameTests::obsidilithDeath)
}