package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.general.data.HistoricalData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class DamageMemory(hitsToRemember: Int, private val entity: LivingEntity) : IDamageHandler {
    private val defaultDamageHistory = DamageHistory(0f, entity.world.damageSources.outOfWorld(), 0)
    private val historicalData = HistoricalData(defaultDamageHistory, hitsToRemember)

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val minimumDamageToNotice = 4
        if(result && damageSource.attacker != null && amount > minimumDamageToNotice) {
            historicalData.set(DamageHistory(amount, damageSource, entity.age))
        }
    }

    fun getDamageHistory() = historicalData.getAll()

    data class DamageHistory(val amount: Float, val source: DamageSource, val ageWhenDamaged: Int)
}