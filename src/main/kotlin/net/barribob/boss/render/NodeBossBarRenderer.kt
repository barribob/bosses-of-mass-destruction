package net.barribob.boss.render

import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.hud.BossBarHud
import net.minecraft.entity.boss.BossBar
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Identifier
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class NodeBossBarRenderer(
    private val entityTypeKey: String,
    private val hpPercentages: List<Float>,
    private val noteTexture: Identifier,
    private val textureSize: Int
) {
    /**
     * Sourced from [BossBarHud.renderBossBar]
     */
    fun renderBossBar(texture: Identifier, drawContext: DrawContext, x: Int, y: Int, bossBar: BossBar, callbackInfo: CallbackInfo) {
        val name = bossBar.name ?: return
        val barContent = name.content
        if (barContent is TranslatableTextContent && barContent.key.equals(entityTypeKey)) {
            val colorLocation = bossBar.color.ordinal * 5 * 2f
            drawContext.drawTexture(
                texture, x, y, 0f, colorLocation, 182, 5,
                textureSize,
                textureSize
            )

            val i = (bossBar.percent * 183.0f).toInt()
            if (i > 0) {
                val progressLocation = bossBar.color.ordinal * 5 * 2 + 5f
                drawContext.drawTexture(
                    texture, x, y, 0f, progressLocation, i, 5,
                    textureSize,
                    textureSize
                )
            }

            renderBossNodes(bossBar, drawContext, x, y)

            callbackInfo.cancel()
        }
    }

    private fun renderBossNodes(
        bossBar: BossBar,
        drawContext: DrawContext,
        x: Int,
        y: Int,
    ) {
        val steppedPercentage = (192 * MathUtils.roundedStep(bossBar.percent, hpPercentages, true)).toInt() + 7
        drawContext.drawTexture(
            noteTexture, x - 3, y - 1, 0f, 0f, steppedPercentage, 7,
            textureSize,
            textureSize
        )

        val steppedPercentageReverse = 192 - steppedPercentage
        drawContext.drawTexture(
            noteTexture,
            x - 3 + steppedPercentage,
            y - 1,
            steppedPercentage.toFloat(),
            7f,
            steppedPercentageReverse,
            7,
            textureSize,
            textureSize
        )
    }
}