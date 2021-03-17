package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.maelstrom.general.event.EventScheduler

class GauntletAttacks(val entity: GauntletEntity, eventScheduler: EventScheduler) {
    private val statusRegistry = mapOf(
        Pair(punchAttack, SwirlPunchAction(entity, eventScheduler)),
        Pair(poundAttack, PoundAction(entity, eventScheduler)),
        Pair(laserAttack, LaserAction(entity, eventScheduler)),
        Pair(swirlPunchAttack, SwirlPunchAction(entity, eventScheduler))
    )
    private val moveLogic = GauntletMoveLogic(statusRegistry, entity)

    fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(moveLogic, 80)
        val onCancel = {
            entity.world.sendEntityStatus(entity, stopAttackAnimation)
            attackAction.stop()
        }
        return ActionGoal(
            ::canContinueAttack,
            tickAction = attackAction,
            endAction = onCancel
        )
    }

    private fun canContinueAttack() = entity.isAlive && entity.target != null

    companion object Status {
        const val punchAttack: Byte = 4
        const val stopPunchAnimation: Byte = 5
        const val stopAttackAnimation: Byte = 6
        const val poundAttack: Byte = 7
        const val stopPoundAnimation: Byte = 8
        const val laserAttack: Byte = 9
        const val laserAttackStop: Byte = 10
        const val swirlPunchAttack: Byte = 11
    }
}