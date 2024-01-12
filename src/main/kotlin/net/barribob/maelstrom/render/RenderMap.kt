package net.barribob.maelstrom.render

import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

class RenderMap {
    val renderMap = hashSetOf<(RenderData) -> Unit>()

    fun render(
        matrices: MatrixStack,
        tickDelta: Float,
        limitTime: Long,
        renderBlockOutline: Boolean,
        camera: Camera,
        gameRenderer: GameRenderer,
        lightmapTextureManager: LightmapTextureManager,
        matrix4f: Matrix4f
    ) {
        val data = RenderData(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f)
        renderMap.forEach { it(data) }
    }
}