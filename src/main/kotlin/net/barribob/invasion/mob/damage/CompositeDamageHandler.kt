package net.barribob.invasion.mob.damage

import net.barribob.invasion.mob.utils.IEntityStats
import net.minecraft.entity.damage.DamageSource

class CompositeDamageHandler(val handlers: List<IDamageHandler>): IDamageHandler {
    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        handlers.forEach { it.beforeDamage(stats, damageSource, amount) }
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        handlers.forEach { it.afterDamage(stats, damageSource, amount) }
    }
}