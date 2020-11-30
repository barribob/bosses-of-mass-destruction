package net.barribob.invasion.mob.ai.action

class CooldownAction(private val action: IActionWithCooldown, private val initialCooldown: Int): IAction, IActionStop {
    var currentTime: Int = initialCooldown

    override fun perform() {
        currentTime--

        if (currentTime <= 0) {
            currentTime = action.perform()
        }
    }

    override fun stop() {
        currentTime = currentTime.coerceAtLeast(initialCooldown)
    }
}