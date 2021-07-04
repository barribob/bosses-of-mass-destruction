package net.barribob.boss.utils

import io.netty.buffer.Unpooled
import net.barribob.boss.Mod
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.maelstrom.static_utilities.readVec3d
import net.barribob.maelstrom.static_utilities.writeVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class NetworkUtils {

    companion object {
        private val spawnEntityPacketId = Mod.identifier("spawn_entity")
        private val changeHitboxPacketId = Mod.identifier("change_hitbox")
        private val gauntletBlindnessPacketId = Mod.identifier("gauntlet_blindness")
        private val playerVelocityPacketId = Mod.identifier("player_velocity")
        private val voidBlossomSpikePacketId = Mod.identifier("void_blossom_spikes")

        fun LivingEntity.sendVelocity(velocity: Vec3d) {
            this.velocity = velocity
            if(this is ServerPlayerEntity) {
                val packet = PacketByteBuf(Unpooled.buffer())
                packet.writeVec3d(velocity)
                ServerPlayNetworking.send(this, playerVelocityPacketId, packet)
            }
        }

        fun GauntletEntity.changeHitbox(open: Boolean) {
            val packet = PacketByteBuf(Unpooled.buffer())
            packet.writeInt(this.id)
            packet.writeBoolean(open)
            PlayerLookup.tracking(this).forEach {
                ServerPlayNetworking.send(it, changeHitboxPacketId, packet)
            }
        }

        fun GauntletEntity.sendBlindnessPacket(players: List<PlayerEntity>) {
            val packet = PacketByteBuf(Unpooled.buffer())
            packet.writeInt(this.id)
            packet.writeIntArray(players.map { it.id }.toIntArray())
            PlayerLookup.tracking(this).forEach {
                ServerPlayNetworking.send(it, gauntletBlindnessPacketId, packet)
            }
        }

        fun VoidBlossomEntity.sendSpikePacket(spikesPositions: List<BlockPos>) {
            for(spikePos in spikesPositions) {
                val buf: PacketByteBuf = PacketByteBufs.create()
                buf.writeInt(this.id)
                buf.writeInt(spikePos.x)
                buf.writeInt(spikePos.y)
                buf.writeInt(spikePos.z)
                for (player in PlayerLookup.tracking(this)) {
                    ServerPlayNetworking.send(player, voidBlossomSpikePacketId, buf)
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(spawnEntityPacketId) { client, _, buf, _ ->
            handleSpawnClientEntity(client, buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(playerVelocityPacketId) { client, _, buf, _ ->
            handlePlayerVelocity(client, buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(changeHitboxPacketId) { client, _, buf, _ ->
            handleChangeHitbox(client, buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(gauntletBlindnessPacketId) { client, _, buf, _ ->
            handleGauntletBlindness(client, buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(voidBlossomSpikePacketId) { client, _, buf, _ ->
            handleVoidBlossomSpikes(client, buf)
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handlePlayerVelocity(client: MinecraftClient, buf: PacketByteBuf) {
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
        return ServerPlayNetworking.createS2CPacket(spawnEntityPacketId, packSpawnClientEntity(
            EntitySpawnS2CPacket(entity)))
    }

    @Environment(EnvType.CLIENT)
    private fun handleSpawnClientEntity(client: MinecraftClient, buf: PacketByteBuf) {
        val packet = EntitySpawnS2CPacket(buf)

        client.execute { VanillaCopies.handleClientSpawnEntity(client, packet) }
    }

    @Environment(EnvType.CLIENT)
    private fun handleChangeHitbox(client: MinecraftClient, buf: PacketByteBuf) {
        val entityId = buf.readInt()
        val open = buf.readBoolean()

        client.execute {
            val entity = client.world?.getEntityById(entityId)
            if(entity is GauntletEntity) {
                if(open) entity.hitboxHelper.setOpenHandHitbox() else entity.hitboxHelper.setClosedFistHitbox()
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleGauntletBlindness(client: MinecraftClient, buf: PacketByteBuf) {
        val entityId = buf.readInt()
        val playerIds = buf.readIntArray()

        client.execute {
            val entity = client.world?.getEntityById(entityId)
            val players: List<PlayerEntity> = playerIds.map { client.world?.getEntityById(it) }.filterIsInstance<PlayerEntity>()
            if(entity is GauntletEntity) {
                entity.clientBlindnessHandler.handlePlayerEffects(players)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private fun handleVoidBlossomSpikes(client: MinecraftClient, buf: PacketByteBuf) {
        val entityId = buf.readInt()
        val spikePos = BlockPos(buf.readInt(), buf.readInt(), buf.readInt())

        client.execute {
            val entity = client.world?.getEntityById(entityId)

            if(entity is VoidBlossomEntity) {
                entity.clientSpikeHandler.addSpike(spikePos)
            }
        }
    }
}