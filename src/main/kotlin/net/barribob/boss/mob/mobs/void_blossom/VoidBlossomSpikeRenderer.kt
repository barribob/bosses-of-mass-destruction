package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.render.IRenderer
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import kotlin.math.*

class VoidBlossomSpikeRenderer : IRenderer<VoidBlossomEntity> {
    private val spikeTexture = Mod.identifier("textures/entity/void_blossom_spike.png")
    private val layer: RenderLayer = RenderLayer.getEntityCutoutNoCull(spikeTexture)
    private val textureRatio = 22.0f / 64.0f

    override fun render(
        entity: VoidBlossomEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        for(kv in entity.clientSpikeHandler.getSpikes()) {
            renderBeam(
                entity,
                kv.value,
                partialTicks,
                ModColors.WHITE,
                matrices,
                vertexConsumers,
                layer
            )
        }
    }

    private fun renderBeam(actor: LivingEntity, spike: VoidBlossomClientSpikeHandler.Spike, tickDelta: Float, color: Vec3d, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, layer: RenderLayer) {
        val numTextures = 6.0f
        val lifeRatio = 3.0f
        val textureProgress = max(0f, ((spike.age + tickDelta) * lifeRatio / spike.maxAge) - lifeRatio + 1)
        if(textureProgress >= 1) return

        val spikeHeight = spike.height
        val spikeWidth = textureRatio * spikeHeight * 0.5f
        val upProgress = (sin((min((spike.age + tickDelta) / (spike.maxAge * 0.2), 1.0)) * Math.PI * 0.5) - 1) * spikeHeight
        val texTransformer = textureMultiplier(1 / numTextures, floor(textureProgress * numTextures) / numTextures)
        matrixStack.push()
        val offset = VanillaCopies.fromLerpedPosition(actor, 0.0, tickDelta).subtract(spike.pos)
        matrixStack.translate(-offset.x, upProgress - offset.y, -offset.z)
        val bottomPos = spike.offset
        val n = acos(bottomPos.y).toFloat()
        val o = atan2(bottomPos.z, bottomPos.x).toFloat()
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((1.5707964f - o) * 57.295776f))
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(n * 57.295776f))
        val q = 0.0F
        val red = (color.x * 255).toInt()
        val green = (color.y * 255).toInt()
        val blue = (color.z * 255).toInt()
        val af = MathHelper.cos(q + 3.1415927f) * spikeWidth
        val ag = MathHelper.sin(q + 3.1415927f) * spikeWidth
        val ah = MathHelper.cos(q + 0.0f) * spikeWidth
        val ai = MathHelper.sin(q + 0.0f) * spikeWidth
        val aj = MathHelper.cos(q + 1.5707964f) * spikeWidth
        val ak = MathHelper.sin(q + 1.5707964f) * spikeWidth
        val al = MathHelper.cos(q + 4.712389f) * spikeWidth
        val am = MathHelper.sin(q + 4.712389f) * spikeWidth
        val vertexConsumer: VertexConsumer = vertexConsumerProvider.getBuffer(layer)
        val entry: MatrixStack.Entry = matrixStack.peek()
        val matrix4f = entry.model
        val matrix3f = entry.normal
        val c0 = texTransformer(0.4999f)
        val c2 = texTransformer(0.0f)
        val c1 = texTransformer(1.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, af, spikeHeight, ag, red, green, blue, c0, 0.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, af, 0.0f, ag, red, green, blue, c0, 1.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, ah, 0.0f, ai, red, green, blue, c2, 1.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, ah, spikeHeight, ai, red, green, blue, c2, 0.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, aj, spikeHeight, ak, red, green, blue, c1, 0.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, aj, 0.0f, ak, red, green, blue, c1, 1.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, al, 0.0f, am, red, green, blue, c0, 1.0f)
        VanillaCopies.method_23173(vertexConsumer, matrix4f, matrix3f, al, spikeHeight, am, red, green, blue, c0, 0.0f)
        matrixStack.pop()
    }

    private fun textureMultiplier(multiplier: Float, adjustment: Float): (Float) -> Float = fun(texCoord): Float {
        return (texCoord * multiplier) + adjustment
    }
}