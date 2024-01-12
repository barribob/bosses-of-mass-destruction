package net.barribob.maelstrom.render

import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

data class RenderData(
    val matrixStack: MatrixStack,
    val tickDelta: Float,
    val limitTime: Long,
    val renderBlockOutline: Boolean,
    val camera: Camera,
    val gameRenderer: GameRenderer,
    val lightmapTextureManager: LightmapTextureManager,
    val matrix4f: Matrix4f
)