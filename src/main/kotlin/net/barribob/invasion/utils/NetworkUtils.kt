package net.barribob.invasion.utils

import io.netty.buffer.Unpooled
import net.barribob.invasion.Invasions
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket

object NetworkUtils {
    val SPAWN_ENTITY_PACKET_ID = Invasions.identifier("spawn_entity")

    private fun packSpawnClientEntity(packet: EntitySpawnS2CPacket): PacketByteBuf {
        val packetData = PacketByteBuf(Unpooled.buffer())
        packet.write(packetData)
        return packetData
    }

    // Todo: probably temporary since they completely limit what information we can send to the client
    fun createClientEntityPacket(entity: Entity): Packet<*> {
        return ServerSidePacketRegistry.INSTANCE.toPacket(SPAWN_ENTITY_PACKET_ID, packSpawnClientEntity(
            EntitySpawnS2CPacket(entity)))
    }

    fun handleSpawnClientEntity(packetContext: PacketContext, buf: PacketByteBuf) {
        val packet = EntitySpawnS2CPacket()
        packet.read(buf)

        packetContext.taskQueue.execute { VanillaCopies.handleClientSpawnEntity(packetContext, packet) }
    }
}