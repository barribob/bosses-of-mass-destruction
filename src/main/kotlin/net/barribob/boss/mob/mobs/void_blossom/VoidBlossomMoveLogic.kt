package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.ai.action.IActionWithCooldown

class VoidBlossomMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: VoidBlossomEntity) : IActionWithCooldown {
    override fun perform(): Int {
        val moveByte = VoidBlossomAttacks.sporeAttack
        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        entity.world.sendEntityStatus(entity, moveByte)
        return action.perform()
    }
}