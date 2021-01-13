package net.barribob.boss.mob.ai.action

class ActionWithConstantCooldown(private val action: IAction, private val cooldown: Int): IActionWithCooldown {
    override fun perform(): Int {
        action.perform()
        return cooldown
    }
}