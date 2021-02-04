package net.barribob.boss.mob.utils

import net.barribob.boss.mob.ai.action.IActionWithCooldown

interface ISidedCooldownAction: IActionWithCooldown {
    fun handleClientStatus()
}