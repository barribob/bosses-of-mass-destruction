package net.barribob.maelstrom.general.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.util.Identifier

class TestArgumentType(private val testCommand: TestCommand) : ArgumentType<Identifier> {
    override fun parse(reader: StringReader?): Identifier {
        val identifier = Identifier.fromCommandInput(reader)
        testCommand.validate(identifier)
        return identifier
    }
}