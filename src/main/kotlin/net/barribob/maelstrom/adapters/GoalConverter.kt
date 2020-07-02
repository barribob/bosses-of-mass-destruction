package net.barribob.maelstrom.adapters

import net.minecraft.entity.ai.goal.Goal
import java.util.*

class GoalConverter(private val goal: Goal) : IGoal {
    override fun canStart(): Boolean { return goal.canStart() }

    override fun shouldContinue(): Boolean { return goal.shouldContinue() }

    override fun start() { goal.start() }

    override fun stop() { goal.stop() }

    override fun tick() { goal.tick() }

    override fun getControls(): EnumSet<IGoal.Control> { return EnumSet.copyOf(goal.controls?.map { IGoal.Control.values()[it.ordinal] }) }
}