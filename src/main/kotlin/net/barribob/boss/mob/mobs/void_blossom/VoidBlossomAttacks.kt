package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.mobs.gauntlet.GauntletAttacks
import net.barribob.maelstrom.general.event.EventScheduler

class VoidBlossomAttacks(val entity: VoidBlossomEntity, eventScheduler: EventScheduler) {
    private val cancelAttackAction: () -> Boolean = { entity.isDead || entity.target == null }
    private val statusRegistry = mapOf(
        Pair(GauntletAttacks.punchAttack, SpikeAction(entity, eventScheduler, cancelAttackAction)),
    )
    private val moveLogic = VoidBlossomMoveLogic(statusRegistry, entity)

    fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(moveLogic, 80)
        val onCancel = {
            entity.world.sendEntityStatus(entity, GauntletAttacks.stopAttackAnimation)
            attackAction.stop()
        }
        return ActionGoal(
            { !cancelAttackAction() },
            tickAction = attackAction,
            endAction = onCancel
        )
    }

    companion object Status {
        const val spikeAttack: Byte = 4
    }
}