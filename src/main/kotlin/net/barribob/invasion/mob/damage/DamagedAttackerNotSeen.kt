package net.barribob.invasion.mob.damage

import net.barribob.invasion.mob.utils.IEntity
import net.barribob.invasion.mob.utils.IEntityStats
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class DamagedAttackerNotSeen(private val actor: IEntity, private val callback: (LivingEntity) -> Unit): IDamageHandler {
    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        if (actor.target() == null) {
            val attacker = damageSource.attacker
            if (attacker is LivingEntity) {
                callback(attacker)
            }
        }
    }
}