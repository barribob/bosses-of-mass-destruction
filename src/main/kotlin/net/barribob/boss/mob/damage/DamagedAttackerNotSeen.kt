package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class DamagedAttackerNotSeen(private val actor: IEntity, private val callback: (LivingEntity) -> Unit): IDamageHandler {
    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
    }

    // Todo: should this only happen if the lich actually takes damage?
    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        if (actor.target() == null) {
            val attacker = damageSource.attacker
            if (attacker is LivingEntity) {
                callback(attacker)
            }
        }
    }
}