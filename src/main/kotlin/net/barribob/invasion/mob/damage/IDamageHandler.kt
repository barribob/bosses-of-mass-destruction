package net.barribob.invasion.mob.damage

import net.barribob.invasion.mob.utils.IEntityStats
import net.minecraft.entity.damage.DamageSource

interface IDamageHandler {
    fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float)
    fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float)
}