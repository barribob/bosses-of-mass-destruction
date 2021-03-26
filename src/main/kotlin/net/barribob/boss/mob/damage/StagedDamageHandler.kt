package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.damage.DamageSource

class StagedDamageHandler(
    private val hpPercentRageModes: List<Float>,
    private val whenHpBelowThreshold: () -> Unit,
) : IDamageHandler {
    var previousHpRatio = 1.0f

    override fun beforeDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float) {
        previousHpRatio = hpPercent(stats)
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val newHpRatio = hpPercent(stats)
        val firstRageMode = MathUtils.roundedStep(previousHpRatio, hpPercentRageModes)
        val secondRageMode = MathUtils.roundedStep(newHpRatio, hpPercentRageModes)
        if (firstRageMode != secondRageMode) {
            whenHpBelowThreshold()
        }
    }

    private fun hpPercent(stats: IEntityStats) = stats.getHealth() / stats.getMaxHealth()
}