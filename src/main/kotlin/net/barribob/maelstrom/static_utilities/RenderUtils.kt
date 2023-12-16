package net.barribob.maelstrom.static_utilities

import com.mojang.blaze3d.systems.RenderSystem
import net.barribob.maelstrom.render.RenderData
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import org.lwjgl.opengl.GL11

object RenderUtils {
    fun renderPoints(renderData: RenderData, color: List<Float>, points: List<Float>) {
        renderData.matrixStack.push()
        RenderSystem.enableBlend()
        RenderSystem.lineWidth(2.0f)
        GL11.glPointSize(2.0f)
        RenderSystem.depthMask(false)
        renderData.matrixStack.translate(-renderData.camera.pos.x, -renderData.camera.pos.y, -renderData.camera.pos.z)
        val tessellate: Tessellator = Tessellator.getInstance()
        val bufferBuilder: BufferBuilder = tessellate.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR)

        (points.indices step 3).forEach {
            bufferBuilder
                .vertex(renderData.matrixStack.peek().positionMatrix, points[it], points[it + 1], points[it + 2])
                .color(color[0], color[1], color[2], color[3]).next()
            bufferBuilder
                .vertex(renderData.matrixStack.peek().positionMatrix, points[it] + 0.01f, points[it + 1] + 0.01f, points[it + 2] + 0.01f)
                .color(color[0], color[1], color[2], color[3]).next()
        }

        tessellate.draw()

        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        renderData.matrixStack.pop()
    }
}