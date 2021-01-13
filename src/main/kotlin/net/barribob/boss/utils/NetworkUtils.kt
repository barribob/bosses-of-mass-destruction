package net.barribob.boss.utils

import io.netty.buffer.Unpooled
import net.barribob.boss.Mod
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object NetworkUtils {
    val SPAWN_ENTITY_PACKET_ID = Mod.identifier("spawn_entity")
    val CLIENT_TEST_PACKET_ID = Mod.identifier("client_test")

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

    fun testClient(world: World, watchPoint: Vec3d) {
        val packetData = PacketByteBuf(Unpooled.buffer())
        PlayerStream.around(world, watchPoint, 100.0).forEach {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(
                it,
                CLIENT_TEST_PACKET_ID,
                packetData
            )
        }
    }

    @Environment(EnvType.CLIENT)
    fun handleTestClient(packetContext: PacketContext, packetData: PacketByteBuf) {
        packetContext.taskQueue.execute {
            InGameTests.testClientCallback(packetContext)
        }
    }
}