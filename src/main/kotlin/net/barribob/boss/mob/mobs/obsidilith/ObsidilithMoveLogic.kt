package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.LivingEntity

class ObsidilithMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: ObsidilithEntity) : IActionWithCooldown {
    private val moveHistory = HistoricalData<Byte>(0)

    private fun chooseMove(): Byte {
        val target = entity.target
        if(target !is LivingEntity) return ObsidilithUtils.burstAttackStatus

        val random = WeightedRandom<Byte>()
        val distanceToTarget = target.squaredDistanceTo(entity)
        val burstWeight = if(distanceToTarget < 36) 1.0 else 0.0
        val anvilWeight = if(distanceToTarget < 36 || moveHistory.getAll().contains(ObsidilithUtils.anvilAttackStatus)) 0.0 else 1.0
        val waveWeight = if(distanceToTarget < 36) 0.5 else 1.0
        val spikeWeight = if(distanceToTarget < 36) 0.0 else 1.0

        random.add(burstWeight, ObsidilithUtils.burstAttackStatus)
        random.add(anvilWeight, ObsidilithUtils.anvilAttackStatus)
        random.add(spikeWeight, ObsidilithUtils.spikeAttackStatus)
        random.add(waveWeight, ObsidilithUtils.waveAttackStatus)

        val nextMove = random.next()
        moveHistory.set(nextMove)

        return nextMove
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        entity.world.sendEntityStatus(entity, moveByte)
        return (actions[moveByte] ?: error("$moveByte action not registered as an attack")).perform()
    }
}