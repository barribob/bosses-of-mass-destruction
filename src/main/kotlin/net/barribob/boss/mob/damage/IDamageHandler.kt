package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

interface IDamageHandler {
    fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float)
    fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float)
    fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean = true
}