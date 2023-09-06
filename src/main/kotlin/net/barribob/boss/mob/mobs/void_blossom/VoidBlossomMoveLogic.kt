package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.ai.TargetSwitcher
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.maelstrom.general.random.WeightedRandom
import net.minecraft.entity.LivingEntity

class VoidBlossomMoveLogic(private val actions: Map<Byte, IActionWithCooldown>, val entity: VoidBlossomEntity, private val doBlossom: () -> Boolean, private val targetSwitcher: TargetSwitcher) : IActionWithCooldown {
    override fun perform(): Int {
        targetSwitcher.trySwitchTarget()
        val target = entity.target
        if(target !is LivingEntity) return 20
        val healthPercentage = entity.health / entity.maxHealth
        val random = WeightedRandom<Byte>()
        val shortDistanceRate = if(target.distanceTo(entity) > 21) 0.0 else 1.0
        val spikeWeight = 1.0
        val sporeWeight = if(healthPercentage < VoidBlossomEntity.hpMilestones[3]) shortDistanceRate else 0.0
        val bladeWeight = if(healthPercentage < VoidBlossomEntity.hpMilestones[2]) 1.0 else 0.0

        val moveByte = if (doBlossom()) {
            VoidBlossomAttacks.blossomAction
        } else {
            random.add(spikeWeight, VoidBlossomAttacks.spikeAttack)
            random.add(shortDistanceRate, VoidBlossomAttacks.spikeWaveAttack)
            random.add(sporeWeight, VoidBlossomAttacks.sporeAttack)
            random.add(bladeWeight, VoidBlossomAttacks.bladeAttack)

            random.next()
        }

        val action = actions[moveByte] ?: error("$moveByte action not registered as an attack")
        entity.world.sendEntityStatus(entity, moveByte)
        return action.perform()
    }
}