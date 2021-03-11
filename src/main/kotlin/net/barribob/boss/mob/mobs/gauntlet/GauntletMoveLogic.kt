package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.action.IActionWithCooldown

class GauntletMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: GauntletEntity) : IActionWithCooldown {
    private fun chooseMove(): Byte {
        return GauntletAttacks.poundAttack
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        entity.world.sendEntityStatus(entity, moveByte)
        return action.perform()
    }
}