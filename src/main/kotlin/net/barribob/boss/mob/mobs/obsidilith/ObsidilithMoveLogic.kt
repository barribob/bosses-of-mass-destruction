package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.IActionWithCooldown

class ObsidilithMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: ObsidilithEntity) : IActionWithCooldown {
    private fun chooseMove(): Byte {
        return ObsidilithUtils.burstAttackStatus
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        entity.world.sendEntityStatus(entity, moveByte)
        return (actions[moveByte] ?: error("$moveByte action not registered as an attack")).perform()
    }
}