package net.barribob.maelstrom.adapters

import java.util.*

/**
 * Adapted from 1.16 Snapshot - Fabric
 */
interface IGoal {
    fun canStart(): Boolean

    fun shouldContinue(): Boolean {
        return canStart()
    }

    fun start() {}

    fun stop() {}

    fun tick() {}

    fun getControls(): EnumSet<Control>? {
        return EnumSet.noneOf(Control::class.java)
    }

    enum class Control { MOVE, LOOK, JUMP, TARGET }
}