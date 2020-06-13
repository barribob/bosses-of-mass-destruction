package net.barribob.maelstrom.adapters

import net.minecraft.entity.ai.goal.Goal
import java.util.*

class GoalAdapter(private val goal: IGoal) : Goal() {
    override fun canStart(): Boolean { return goal.canStart() }

    override fun shouldContinue(): Boolean { return goal.shouldContinue() }

    override fun start() { goal.start() }

    override fun stop() { goal.stop() }

    override fun tick() { goal.tick() }

    override fun getControls(): EnumSet<Control> { return EnumSet.copyOf(goal.getControls()?.map { Control.values()[it.ordinal] }) }
}