package net.barribob.invasion.mob.ai.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestCooldownAction {
    @Test
    fun stop_ResetsToInitialCooldown() {
        val action = createCooldownAction(cooldown = 10)
        action.currentTime = 1
        action.stop()
        Assertions.assertEquals(action.currentTime, 10)
    }

    @Test
    fun perform_AfterCurrentTimeTicks_PerformsAction() {
        var fired = 0
        val cooldown = 2
        val action = createCooldownAction({
            fired++
            cooldown
        }, cooldown)

        action.perform()
        action.perform()

        Assertions.assertEquals(fired, 1)
    }

    @Test
    fun perform_ResetsToCooldown_AfterPerformsAction() {
        val cooldown = 10
        val action = createCooldownAction({ cooldown }, 0)

        action.perform()

        Assertions.assertEquals(action.currentTime, 10)
    }

    private fun createCooldownAction(action: IActionWithCooldown = IActionWithCooldown{ 1 }, cooldown: Int = 1) = CooldownAction(action, cooldown)
}