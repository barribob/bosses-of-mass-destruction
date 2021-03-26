package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class ObsidilithMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: ObsidilithEntity) : IActionWithCooldown, IDamageHandler {
    private val moveHistory = HistoricalData<Byte>(0)
    private var shouldDoPillarDefense = false
    private val damageHandler = StagedDamageHandler(ObsidilithUtils.hpPillarShieldMilestones) {
        shouldDoPillarDefense = true
    }

    private fun chooseMove(): Byte {
        val target = entity.target
        if(target !is LivingEntity) return ObsidilithUtils.burstAttackStatus

        val nextMove = if (shouldDoPillarDefense) {
            shouldDoPillarDefense = false
            ObsidilithUtils.pillarDefenseStatus
        } else {
            val random = WeightedRandom<Byte>()
            val distanceToTarget = target.squaredDistanceTo(entity)
            val burstWeight = if (distanceToTarget < 36) 1.0 else 0.0
            val anvilWeight = if (distanceToTarget < 36 || moveHistory.getAll()
                    .contains(ObsidilithUtils.anvilAttackStatus)
            ) 0.0 else 1.0
            val waveWeight = if (distanceToTarget < 36) 0.5 else 1.0
            val spikeWeight = if (distanceToTarget < 36) 0.0 else 1.0

            random.add(burstWeight, ObsidilithUtils.burstAttackStatus)
            random.add(anvilWeight, ObsidilithUtils.anvilAttackStatus)
            random.add(spikeWeight, ObsidilithUtils.spikeAttackStatus)
            random.add(waveWeight, ObsidilithUtils.waveAttackStatus)

            random.next()
        }

        moveHistory.set(nextMove)

        return nextMove
    }

    override fun perform(): Int {
        val moveByte = chooseMove()
        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        entity.world.sendEntityStatus(entity, moveByte)
        return action.perform()
    }

    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        damageHandler.beforeDamage(stats, damageSource, amount)
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        damageHandler.afterDamage(stats, damageSource, amount, result)
    }
}