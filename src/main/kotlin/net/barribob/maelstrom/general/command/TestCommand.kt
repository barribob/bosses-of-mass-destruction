package net.barribob.maelstrom.general.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.InGameTests
import net.barribob.maelstrom.static_utilities.format
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.command.suggestion.SuggestionProviders
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import kotlin.system.measureNanoTime

class TestCommand(inGameTests: InGameTests) : CommandRegistrationCallback {
    private val notFoundException = DynamicCommandExceptionType { Text.literal("Test name not found") }

    private val tests = mutableMapOf<Identifier, (ServerCommandSource) -> Unit>()

    private val nameArgumentName = "name"

    init {
        addId(inGameTests::lineCallback.name, inGameTests::lineCallback)
        addId(inGameTests::boxCorners.name, inGameTests::boxCorners)
        addId(inGameTests::willBoxFit.name, inGameTests::willBoxFit)
        addId(inGameTests::raycast.name, inGameTests::raycast)
        addId(inGameTests::circleCallback.name, inGameTests::circleCallback)
    }

    private val suggestions: SuggestionProvider<ServerCommandSource> =
        SuggestionProviders.register(
            Identifier(MaelstromMod.MODID, "test"),
            SuggestionProvider { _, builder ->
                CommandSource.forEachMatching(
                    tests.keys,
                    builder.remaining.lowercase(Locale.ROOT),
                    { it },
                    { builder.suggest(it.toString()) })
                return@SuggestionProvider builder.buildFuture()
            })

    fun addId(
        name: String,
        callback: (ServerCommandSource) -> Unit
    ) = tests.put(Identifier(name.lowercase(Locale.ROOT)), callback)

    override fun register(dispatcher: CommandDispatcher<ServerCommandSource>, registryAccess: CommandRegistryAccess?, environment: CommandManager.RegistrationEnvironment?) {
        val commandName = "libtest"
        val timeArgumentName = "ticks"
        dispatcher.register(
            CommandManager.literal(commandName).then(
                CommandManager.argument(nameArgumentName, TestArgumentType(this))
                    .suggests(suggestions)
                    .executes { run(it) }
                    .then(
                        CommandManager.argument(timeArgumentName, IntegerArgumentType.integer(1))
                            .executes { run(it, IntegerArgumentType.getInteger(it, timeArgumentName)) }
                    )
            )
        )
    }

    private fun run(context: CommandContext<ServerCommandSource>, ticks: Int = 1): Int {
        val identifier = context.getArgument(nameArgumentName, Identifier::class.java)
        validate(identifier)
        var time = 0L

        val runTest: () -> Unit = {
            try {
                time += measureNanoTime { tests[identifier]?.invoke(context.source) }
            } catch (e: Exception) {
                context.source.sendFeedback({Text.literal(e.message)}, false)
                e.printStackTrace()
            }
        }

        val eventScheduler = ModComponents.getWorldEventScheduler(context.source.world)
        eventScheduler.addEvent(TimedEvent(runTest, 0, ticks))
        eventScheduler.addEvent(TimedEvent({
            context.source.sendFeedback(
                {Text.literal("Test(s) ran using ${((time / ticks) * 1e-6).format(3)} ms of runtime")},
                false
            )
        }, ticks))
        

        return 1
    }

    fun validate(identifier: Identifier) {
        if (!tests.containsKey(identifier)) {
            throw notFoundException.create(identifier)
        }
    }
}