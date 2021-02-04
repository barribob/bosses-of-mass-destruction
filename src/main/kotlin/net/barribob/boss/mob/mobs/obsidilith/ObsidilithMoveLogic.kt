package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ISidedCooldownAction

class ObsidilithMoveLogic(private val actions: Map<Byte, ISidedCooldownAction>) : IActionWithCooldown {
    private fun chooseMove(): Byte {
        return ObsidilithUtils.burstAttackStatus
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        return (actions[moveByte] ?: error("$moveByte action not registered as an attack")).perform()
    }
}