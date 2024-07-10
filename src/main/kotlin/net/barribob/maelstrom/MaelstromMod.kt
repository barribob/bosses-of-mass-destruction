package net.barribob.maelstrom

import net.barribob.boss.Mod
import net.barribob.maelstrom.MaelstromMod.isDevelopmentEnvironment
import net.barribob.maelstrom.general.command.TestArgumentType
import net.barribob.maelstrom.general.command.TestCommand
import net.barribob.maelstrom.general.io.ConsoleLogger
import net.barribob.maelstrom.general.io.ILogger
import net.barribob.maelstrom.mixin.ArgumentTypeAccessor
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object MaelstromMod {
    internal const val MODID = Mod.MODID

    val LOGGER: ILogger = ConsoleLogger(LogManager.getLogger())
    val testCommand = TestCommand()

    val isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment
}

fun init() {
    if(isDevelopmentEnvironment){
        initDev()
    }
}

private fun initDev() {
    CommandRegistrationCallback.EVENT.register(MaelstromMod.testCommand)
    ArgumentTypeAccessor.register(Registries.COMMAND_ARGUMENT_TYPE,
        "${MaelstromMod.MODID}:libtest",
        TestArgumentType::class.java,
        ConstantArgumentSerializer.of { _ -> TestArgumentType(MaelstromMod.testCommand) })
}