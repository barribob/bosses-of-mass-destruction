package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.render.IRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld

class EternalNightRenderer: IRenderer<LichEntity> {
    override fun render(
        entity: LichEntity,
        yaw: Float,
        partialTicks: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
    ) {
        val world = entity.world
        val isAddedToWorld = world.getEntityById(entity.id) != null
        if (entity.shouldSetToNighttime && world is ClientWorld && isAddedToWorld) {
            world.timeOfDay = LichUtils.timeToNighttime(world.timeOfDay)
        }
    }
}