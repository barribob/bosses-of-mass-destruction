package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.IActionWithCooldown

class ObsidilithMoveLogic(private val actions: Map<Byte, IActionWithCooldown>) : IActionWithCooldown {
    private fun chooseMove(): Byte {
        return ObsidilithUtils.waveAttackStatus
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        return (actions[moveByte] ?: error("$moveByte action not registered as an attack")).perform()
    }
}