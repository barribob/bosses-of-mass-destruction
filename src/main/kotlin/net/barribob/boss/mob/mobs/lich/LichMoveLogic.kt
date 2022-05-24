package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.ai.TargetSwitcher
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class LichMoveLogic(
    private val actions: Map<Byte, IActionWithCooldown>,
    private val actor: LichEntity,
    damageMemory: DamageMemory
): IDamageHandler, IActionWithCooldown, IEntityTick<ServerWorld> {
    private val damageHistory = HistoricalData(0, 3)
    private val moveHistory = HistoricalData<Byte>(0, 4)
    private val positionalHistory = HistoricalData(Vec3d.ZERO, 10)
    private val priorityMoves = mutableListOf<Byte>()
    private val stagedDamageHandler = StagedDamageHandler(LichUtils.hpPercentRageModes) {
        priorityMoves.addAll(listOf(
            LichActions.cometRageAttack, LichActions.volleyRageAttack, LichActions.minionRageAttack
        ))
    }
    private val targetSwitcher = TargetSwitcher(actor, damageMemory)

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        damageHistory.set(actor.age)
        stagedDamageHandler.afterDamage(stats, damageSource, amount, result)
    }

    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        stagedDamageHandler.beforeDamage(stats, damageSource, amount)
    }

    override fun perform(): Int {
        val moveByte = if (priorityMoves.any()) {
            priorityMoves.removeFirst()
        } else {
            chooseRegularMove()
        }
        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        actor.world.sendEntityStatus(actor, moveByte)
        return action.perform()
    }

    private fun chooseRegularMove(): Byte {
        targetSwitcher.trySwitchTarget()
        val random = WeightedRandom<Byte>()
        val teleportWeight = getTeleportWeight()
        val minionWeight = if (moveHistory.getAll().contains(LichActions.minionAttack)) 0.0 else 2.0

        random.addAll(
            listOf(
                Pair(1.0, LichActions.cometAttack),
                Pair(1.0, LichActions.volleyAttack),
                Pair(minionWeight, LichActions.minionAttack),
                Pair(teleportWeight, LichActions.teleportAction)
            )
        )

        val nextMove = random.next()
        moveHistory.set(nextMove)

        return nextMove
    }

    private fun getTeleportWeight(): Double {
        val damage = damageHistory.getAll()
        val hasBeenRapidlyDamaged = damage.size > 2 && damage.last() - damage.first() < 60
        val target = actor.target ?: return 0.0
        return  (if (actor.inLineOfSight(target)) 0.0 else 4.0) +
                (if (actor.pos.distanceTo(target.pos) < 6.0) 8.0 else 0.0) +
                (if (hasBeenRapidlyDamaged) 8.0 else 0.0)
    }

    override fun tick(world: ServerWorld) {
        positionalHistory.set(actor.pos)
    }
}