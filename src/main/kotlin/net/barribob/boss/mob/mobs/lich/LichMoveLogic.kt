package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.utils.IEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.data.IHistoricalData
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.math.Vec3d

class LichMoveLogic(
    private val cometThrowAction: IActionWithCooldown,
    private val missileAction: IActionWithCooldown,
    private val minionAction: IActionWithCooldown,
    private val teleportAction: IActionWithCooldown,
    private val positionalHistory: () -> IHistoricalData<Vec3d>,
    private val moveHistory: () -> IHistoricalData<IActionWithCooldown>,
    private val inLineOfSight: (Vec3d, IEntity) -> Boolean,
    private val actor: IEntity,
    private val target: IEntity
): IDamageHandler {
    private val damageHistory = HistoricalData(0, 3)
    fun chooseRegularMove(): IActionWithCooldown {
        val random = WeightedRandom<IActionWithCooldown>()
        val (teleportWeight, minionWeight) = getWeights()

        random.addAll(
            listOf(
                Pair(1.0, cometThrowAction),
                Pair(1.0, missileAction),
                Pair(minionWeight, minionAction),
                Pair(teleportWeight, teleportAction)
            )
        )
        return random.next()
    }

    private fun getWeights(): Pair<Double, Double> {
        val distanceTraveled = positionalHistory().getAll().zipWithNext()
            .fold(0.0) { acc, pair -> acc + pair.first.distanceTo(pair.second) }
        val damage = damageHistory.getAll()
        val hasBeenRapidlyDamaged = damage.size > 2 && damage.last() - damage.first() < 60
        val teleportWeight = 0.0 +
                (if (inLineOfSight(actor.getEyePos(), target)) 0.0 else 4.0) +
                (if (distanceTraveled > 0.25) 0.0 else 8.0) +
                (if (actor.getPos().distanceTo(target.getPos()) < 6.0) 8.0 else 0.0) +
                (if (hasBeenRapidlyDamaged) 8.0 else 0.0)
        val minionWeight = if (moveHistory().getAll().contains(minionAction)) 0.0 else 2.0
        return Pair(teleportWeight, minionWeight)
    }

    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        damageHistory.set(actor.getAge())
    }
}