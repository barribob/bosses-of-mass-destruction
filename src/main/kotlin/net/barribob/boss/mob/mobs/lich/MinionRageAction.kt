package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.network.ServerPlayerEntity

class MinionRageAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean,
    private val minionAction: MinionAction
) : IActionWithCooldown {
    private val delayTimes = (0 until numMobs)
        .map { MathUtils.consecutiveSum(0, it) }
        .mapIndexed { index, i -> initialSpawnTimeCooldown + (index * initialBetweenSpawnDelay) - (i * spawnDelayDecrease) }
    private val totalMoveTime = delayTimes.last() + MinionAction.minionRuneToMinionSpawnDelay

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return totalMoveTime
        performMinionSummon(target)
        return totalMoveTime
    }

    private fun performMinionSummon(target: ServerPlayerEntity) {
        for (delayTime in delayTimes) {
            eventScheduler.addEvent(TimedEvent({ minionAction.beginSummonSingleMob(target) },
                delayTime, shouldCancel = shouldCancel))
        }
    }

    companion object {
        private const val numMobs = 9
        private const val initialSpawnTimeCooldown = 40
        private const val initialBetweenSpawnDelay = 40
        private const val spawnDelayDecrease = 3
    }
}