package net.barribob.invasion.static_utilities

import io.netty.buffer.Unpooled
import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.maelstrom.MaelstromMod
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

object Animations {
    fun startAnimation(entity: BaseEntity, animationId: String) {
        val animId = entity.geckoManager.idRegistry.getIdFromName(animationId)
        if (animId == null) {
            MaelstromMod.LOGGER.error("No animation registered for animation name: $animationId")
            return
        }

        val packetData = PacketByteBuf(Unpooled.buffer())
        packetData.writeInt(entity.entityId)
        packetData.writeInt(animId)
        PlayerStream.watching(entity).forEach {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(
                it,
                Invasions.START_ANIMATION_PACKET_ID,
                packetData
            )
        }
    }

    @Environment(EnvType.CLIENT)
    fun startAnimationClient(packetContext: PacketContext, packetData: PacketByteBuf) {
        val entityId = packetData.readInt()
        val animId = packetData.readInt()

        packetContext.taskQueue.execute {
            // Todo: Does this packet have a race condition with the entity creation packet?
            val entity = MinecraftClient.getInstance().world?.getEntityById(entityId)

            if (entity is BaseEntity) {
                val animName = entity.geckoManager.idRegistry.getNameFromId(animId)
                if (animName == null) {
                    MaelstromMod.LOGGER.error("Missing animation on the client side for id: $animId")
                    return@execute
                }

                entity.geckoManager.startAnimation(animName)
            }
        }
    }
}