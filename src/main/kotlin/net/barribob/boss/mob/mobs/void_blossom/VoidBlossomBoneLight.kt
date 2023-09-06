package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.render.IBoneLight
import net.barribob.boss.render.IRenderer
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector4f
import software.bernie.geckolib.cache.`object`.GeoBone

class VoidBlossomBoneLight : IBoneLight, IRenderer<VoidBlossomEntity> {
    private var entity: VoidBlossomEntity? = null
    private var partialTicks: Float? = null

    override fun getLightForBone(bone: GeoBone, packedLight: Int): Int {
        return if (bone.name.contains("Spike") || bone.name.contains("Thorn") || bone.name.contains("FlowerCenter")) {
            IBoneLight.fullbright
        } else {
            packedLight
        }
    }

    override fun getColorForBone(bone: GeoBone, rgbaColor: Vector4f): Vector4f {
        val entity = entity ?: return rgbaColor
        val partialTicks = partialTicks ?: 0f
        val newColor = Vector4f(rgbaColor.x, rgbaColor.y, rgbaColor.z, rgbaColor.w)

        if(entity.isDead) {
            val interceptedTime = MathUtils.ratioLerp(entity.deathTime.toFloat(), 0.5f, LightBlockRemover.deathMaxAge, partialTicks)
            newColor.mul(1f - interceptedTime * 0.5f)
        }

        return newColor
    }

    override fun render(
        entity: VoidBlossomEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        this.entity = entity
        this.partialTicks = partialTicks
    }
}