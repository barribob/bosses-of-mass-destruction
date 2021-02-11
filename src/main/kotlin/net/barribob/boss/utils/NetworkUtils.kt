package net.barribob.boss.utils

import io.netty.buffer.Unpooled
import net.barribob.boss.Mod
import net.barribob.boss.particle.ParticleFactories
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class NetworkUtils {
    val SPAWN_ENTITY_PACKET_ID = Mod.identifier("spawn_entity")
    val CLIENT_TEST_PACKET_ID = Mod.identifier("client_test")

    companion object {
        val PLAYER_VELOCITY_ID = Mod.identifier("player_velocity")

        fun LivingEntity.sendVelocity(velocity: Vec3d) {
            this.velocity = velocity
            if(this is ServerPlayerEntity) {
                val packet = PacketByteBuf(Unpooled.buffer())
                packet.writeVec3d(velocity)
                ServerPlayNetworking.send(this, PLAYER_VELOCITY_ID, packet)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun handlePlayerVelocity(client: MinecraftClient, buf: PacketByteBuf) {
        val velocity = buf.readVec3d()
        client.execute {
            client.player?.velocity = velocity
        }
    }

    private fun packSpawnClientEntity(packet: EntitySpawnS2CPacket): PacketByteBuf {
        val packetData = PacketByteBuf(Unpooled.buffer())
        packet.write(packetData)
        return packetData
    }

    fun createClientEntityPacket(entity: Entity): Packet<*> {
        return ServerPlayNetworking.createS2CPacket(SPAWN_ENTITY_PACKET_ID, packSpawnClientEntity(
            EntitySpawnS2CPacket(entity)))
    }

    @Environment(EnvType.CLIENT)
    fun handleSpawnClientEntity(client: MinecraftClient, buf: PacketByteBuf) {
        val packet = EntitySpawnS2CPacket()
        packet.read(buf)

        client.execute { VanillaCopies.handleClientSpawnEntity(client, packet) }
    }

    fun testClient(world: ServerWorld, watchPoint: Vec3d) {
        val packetData = PacketByteBuf(Unpooled.buffer())
        PlayerLookup.around(world, watchPoint, 100.0).forEach {
            ServerPlayNetworking.send(it, CLIENT_TEST_PACKET_ID, packetData)
        }
    }

    @Environment(EnvType.CLIENT)
    fun testClientCallback(client: MinecraftClient) {
        val pos = client.player?.pos ?: return
        val deathParticleFactory = ParticleFactories.soulFlame()
            .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
            .age { RandomUtils.range(40, 80) }
            .colorVariation(0.5)
            .scale { 0.5f - (it * 0.3f) }

        MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
            for(i in 0..4) {
                val particlePos = pos.add(RandomUtils.randVec())
                deathParticleFactory.continuousVelocity {
                    MathUtils.unNormedDirection(it.getPos(), pos).crossProduct(VecUtils.yAxis).multiply(0.1)
                }.build(particlePos)
            }
        }, 0, 10))
    }
}