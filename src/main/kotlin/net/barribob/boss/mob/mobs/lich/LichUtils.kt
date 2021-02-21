package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.mob.utils.IEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.server.world.ServerWorld

object LichUtils {
    val hpPercentRageModes = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    const val textureSize = 256
    val bossBarDividerTexture = Mod.identifier("textures/gui/lich_boss_bar_dividers.png")

    /**
     * Info from [ServerWorld.tick]
     */
    fun timeToNighttime(currentTime: Long): Long {
        val dayLength = 24000L
        val midnight = 16000L
        return (currentTime - (currentTime % dayLength)) + midnight
    }

    fun cappedHeal(
        iEntity: IEntity,
        stats: IEntityStats,
        hpPercentRageModes: List<Float>,
        healingStrength: Float,
        heal: (Float) -> Unit,
    ) {
        if (iEntity.isAlive() && iEntity.target() == null) {
            val targetHealthRatio = MathUtils.roundedStep(stats.getHealth() / stats.getMaxHealth(), hpPercentRageModes)
            if ((stats.getHealth() + healingStrength) / stats.getMaxHealth() < targetHealthRatio) {
                heal(healingStrength)
            }
        }
    }
}