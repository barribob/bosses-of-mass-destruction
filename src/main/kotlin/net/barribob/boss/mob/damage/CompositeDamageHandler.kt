package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class CompositeDamageHandler(vararg handlers: IDamageHandler): IDamageHandler {
    private val handlerList = handlers.toList()

    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        handlerList.forEach { it.beforeDamage(stats, damageSource, amount) }
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        handlerList.forEach { it.afterDamage(stats, damageSource, amount, result) }
    }

    override fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean {
        return handlerList.all { it.shouldDamage(actor, damageSource, amount) }
    }
}