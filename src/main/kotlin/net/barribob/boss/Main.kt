package net.barribob.boss

import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer
import net.barribob.boss.Mod.networkUtils
import net.barribob.boss.Mod.vec3dNetwork
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.config.ModConfig
import net.barribob.boss.item.ModItems
import net.barribob.boss.loot.ModLoot
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.Particles
import net.barribob.boss.sound.ModSounds
import net.barribob.boss.utils.InGameTests
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.NetworkUtils
import net.barribob.boss.utils.Vec3dNetworkHandler
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object Mod {
    const val MODID = "bosses_of_mass_destruction"

    val LOGGER = ConsoleLogger(LogManager.getLogger())

    val structures: ModStructures = ModStructures()
    val sounds: ModSounds = ModSounds()
    val items: ModItems = ModItems()
    val loot: ModLoot = ModLoot()

    val networkUtils = NetworkUtils()
    val vec3dNetwork = Vec3dNetworkHandler()

    fun identifier(path: String) = Identifier(MODID, path)
}

@Suppress("unused")
fun init() {
    networkUtils.registerHandlers()
    vec3dNetwork.registerHandlers()
    AutoConfig.register(ModConfig::class.java, ::JanksonConfigSerializer)
    AutoConfig.getConfigHolder(ModConfig::class.java).config.postInit()
    AutoConfig.getConfigHolder(ModConfig::class.java).save()

    ModBlocks.init()
    Entities.init()

    Mod.items.init()
    Mod.sounds.init()

    if(MaelstromMod.isDevelopmentEnvironment) initDev()
}

@Environment(EnvType.CLIENT)
@Suppress("unused")
fun clientInit() {
    networkUtils.registerClientHandlers()

    Entities.clientInit()
    Particles.clientInit()
    ModBlocks.clientInit()
    Mod.items.clientInit()
    vec3dNetwork.clientInit()

    if(MaelstromMod.isDevelopmentEnvironment) initClientDev()
}

private fun initDev() {
    val inGameTests = InGameTests()
    MaelstromMod.testCommand.addId(inGameTests::throwProjectile.name, inGameTests::throwProjectile)
    MaelstromMod.testCommand.addId(inGameTests::spawnEntity.name, inGameTests::spawnEntity)
    MaelstromMod.testCommand.addId(inGameTests::testClient.name, inGameTests::testClient)
    MaelstromMod.testCommand.addId(inGameTests::burstAction.name, inGameTests::burstAction)
    MaelstromMod.testCommand.addId(inGameTests::placePillars.name, inGameTests::placePillars)
    MaelstromMod.testCommand.addId(inGameTests::obsidilithDeath.name, inGameTests::obsidilithDeath)
    MaelstromMod.testCommand.addId(inGameTests::provideGear.name, inGameTests::provideGear)
    MaelstromMod.testCommand.addId(inGameTests::killZombies.name, inGameTests::killZombies)
    MaelstromMod.testCommand.addId(inGameTests::lichSpawn.name, inGameTests::lichSpawn)
    MaelstromMod.testCommand.addId(inGameTests::levitationPerformance.name, inGameTests::levitationPerformance)
    MaelstromMod.testCommand.addId(inGameTests::wallTeleport.name, inGameTests::wallTeleport)
    MaelstromMod.testCommand.addId(inGameTests::attackRepeatedly.name, inGameTests::attackRepeatedly)
    inGameTests.registerHandlers()
}

@Environment(EnvType.CLIENT)
private fun initClientDev() {
    val inGameTests = InGameTests()
    inGameTests.registerClientHandlers()
}