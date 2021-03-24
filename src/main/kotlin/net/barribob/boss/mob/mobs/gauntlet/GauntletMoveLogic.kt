package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.LivingEntity

class GauntletMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: GauntletEntity) :
    IActionWithCooldown {
    private val moveHistory = HistoricalData<Byte>(0, 4)

    private fun chooseMove(): Byte {
        val target = entity.target
        if (target !is LivingEntity) return GauntletAttacks.punchAttack
        val healthPercentage = entity.health / entity.maxHealth

        val random = WeightedRandom<Byte>()

        val punchWeight = 1.0
        val laserWeight = if (moveHistory.get(0) == GauntletAttacks.laserAttack || healthPercentage >= laserPercentage) 0.0 else 0.7
        val swirlPunchWeight = if (moveHistory.get(0) == GauntletAttacks.swirlPunchAttack || healthPercentage >= swirlPunchPercentage) 0.0 else 0.7
        val blindnessWeight = if(moveHistory.getAll().contains(GauntletAttacks.blindnessAttack) || healthPercentage >= blindnessPercentage) 0.0 else 1.0

        random.add(punchWeight, GauntletAttacks.punchAttack)
        random.add(laserWeight, GauntletAttacks.laserAttack)
        random.add(swirlPunchWeight, GauntletAttacks.swirlPunchAttack)
        random.add(blindnessWeight, GauntletAttacks.blindnessAttack)

        val nextMove = random.next()
        moveHistory.set(nextMove)

        return nextMove
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        entity.world.sendEntityStatus(entity, moveByte)
        return action.perform()
    }

    companion object {
        const val laserPercentage = 0.9
        const val swirlPunchPercentage = 0.8
        const val blindnessPercentage = 0.6
    }
}