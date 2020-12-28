package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.mob.utils.IEntity
import net.barribob.invasion.mob.utils.IEntityStats
import net.barribob.maelstrom.static_utilities.MathUtils

object LichUtils {
    fun cappedHeal(
        iEntity: IEntity,
        stats: IEntityStats,
        hpPercentRageModes: List<Float>,
        healingStrength: Float,
        heal: (Float) -> Unit
    ) {
        if (iEntity.isAlive() && iEntity.target() != null) {
            val targetHealthRatio = MathUtils.roundedStep(stats.getHealth() / stats.getMaxHealth(), hpPercentRageModes)
            if ((stats.getHealth() + healingStrength) / stats.getMaxHealth() < targetHealthRatio) {
                heal(healingStrength)
            }
        }
    }
}