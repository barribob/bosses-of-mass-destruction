package net.barribob.invasion.mob.ai.goals

import net.minecraft.entity.ai.goal.Goal

class CompositeGoal(private val goals: List<Goal>) : Goal() {
    init {
        goals.forEach { goal: Goal -> controls.addAll(goal.controls) }
    }

    override fun canStart(): Boolean = goals.all { it.canStart() }
    override fun canStop(): Boolean = goals.all { it.canStop() }

    override fun tick() {
        goals.forEach { it.tick() }
    }

    override fun stop() {
        goals.forEach { it.stop() }
    }

    override fun start() {
        goals.forEach { it.start() }
    }
}