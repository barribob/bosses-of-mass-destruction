package net.barribob.invasion.mob.mobs.lich

import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.Entities
import net.barribob.invasion.mob.utils.IEntity
import net.barribob.invasion.mob.utils.IEntityStats
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.hud.BossBarHud
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.boss.BossBar
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.TranslatableText
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object LichUtils {
    val hpPercentRageModes = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    private const val textureSize = 256
    private val bossBarDividerTexture = Invasions.identifier("textures/gui/lich_boss_bar_dividers.png")

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

    /**
     * Sourced from [BossBarHud.renderBossBar]
     */
    fun renderBossBar(matrices: MatrixStack, x: Int, y: Int, bossBar: BossBar, callbackInfo: CallbackInfo) {

        val name = bossBar.name
        if (name is TranslatableText && name.key.contains(Entities.LICH.translationKey)) {
            val colorLocation = bossBar.color.ordinal * 5 * 2f
            DrawableHelper.drawTexture(matrices, x, y, 0f, colorLocation, 182, 5, textureSize, textureSize)

            val i = (bossBar.percent * 183.0f).toInt()
            if (i > 0) {
                val progressLocation = bossBar.color.ordinal * 5 * 2 + 5f
                DrawableHelper.drawTexture(matrices, x, y, 0f, progressLocation, i, 5, textureSize, textureSize)
            }

            renderBossNodes(bossBar, matrices, x, y)

            callbackInfo.cancel()
        }
    }

    private fun renderBossNodes(
        bossBar: BossBar,
        matrices: MatrixStack,
        x: Int,
        y: Int,
    ) {
        MinecraftClient.getInstance().textureManager.bindTexture(bossBarDividerTexture)
        val steppedPercentage = (192 * MathUtils.roundedStep(bossBar.percent, hpPercentRageModes, true)).toInt() + 7
        DrawableHelper.drawTexture(matrices, x - 3, y - 1, 0f, 0f, steppedPercentage, 7, textureSize, textureSize)

        val steppedPercentageReverse = 192 - steppedPercentage
        DrawableHelper.drawTexture(matrices,
            x - 3 + steppedPercentage,
            y - 1,
            steppedPercentage.toFloat(),
            7f,
            steppedPercentageReverse,
            7,
            textureSize,
            textureSize)
    }
}