package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.maelstrom.general.data.IHistoricalData

class PrioritizedAttackAction(
    private val priorityMoves: () -> MutableList<IActionWithCooldown>,
    private val defaultMoveProvider: () -> IActionWithCooldown,
    private val moveHistory: IHistoricalData<IActionWithCooldown>
) : IActionWithCooldown {
    override fun perform(): Int {
        val nextMove = if (priorityMoves().isEmpty()) {
            defaultMoveProvider()
        } else {
            priorityMoves().removeFirst()
        }
        moveHistory.set(nextMove)
        return nextMove.perform()
    }
}