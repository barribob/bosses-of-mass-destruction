package net.barribob.boss.mob.ai

import net.barribob.boss.mob.damage.DamageMemory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity

class TargetSwitcher(private val entity: MobEntity, private val damageMemory : DamageMemory) {
    private val ageToRemember = 20 * 30

    fun trySwitchTarget() {
        val newTarget = damageMemory.getDamageHistory()
            .filter (::filterTargetableEntities)
            .filter { h -> h.ageWhenDamaged + ageToRemember >= entity.age }
            .groupBy { h -> h.source.attacker }
            .maxByOrNull { kv -> kv.value.sumOf { h -> h.amount.toDouble() } }?.key

        if(newTarget != null && newTarget != entity.target && entity.random.nextInt(2) == 0) {
            entity.target = newTarget as LivingEntity
        }
    }

    private fun filterTargetableEntities(damageHistory: DamageMemory.DamageHistory): Boolean {
        val attacker = damageHistory.source.attacker
        if (attacker is LivingEntity) {
            val canSee = entity.visibilityCache.canSee(attacker)
            val followRange = entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)
            val inRange = entity.squaredDistanceTo(attacker) < followRange * followRange
            return canSee && inRange && entity.canTarget(attacker)
        }

        return false
    }
}