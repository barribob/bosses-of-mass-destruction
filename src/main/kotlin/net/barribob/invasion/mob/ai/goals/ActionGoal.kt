package net.barribob.invasion.mob.ai.goals

import net.barribob.invasion.mob.ai.action.IAction
import net.barribob.invasion.mob.ai.action.IActionStop
import net.minecraft.entity.ai.goal.Goal

class ActionGoal(
    val hasTarget: () -> Boolean,
    val canContinue: () -> Boolean = hasTarget,
    private val tickAction: IAction = IAction{},
    private val startAction: IAction = IAction{},
    private val endAction: IActionStop = IActionStop{}
) : Goal() {
    override fun canStart(): Boolean = hasTarget()

    override fun shouldContinue(): Boolean = canContinue()

    override fun start() {
        startAction.perform()
    }

    override fun tick() {
        tickAction.perform()
    }

    override fun stop() {
        endAction.stop()
    }
}