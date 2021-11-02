package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.mobs.gauntlet.GauntletAttacks

class LichActions(val entity: LichEntity, private val attackAction: IActionWithCooldown) {
    private val cancelAttackAction: () -> Boolean = { entity.isDead || entity.target == null }

    fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(attackAction, 80)
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

    companion object {
        const val stopAttackAnimation: Byte = 4
        const val cometAttack: Byte = 5
        const val volleyAttack : Byte = 6
        const val minionAttack : Byte = 7
        const val minionRageAttack : Byte = 8
        const val teleportAction : Byte = 9
        const val endTeleport : Byte = 10
        const val volleyRageAttack : Byte = 11
        const val cometRageAttack : Byte = 12
        const val hpBelowThresholdStatus: Byte = 13
    }
}