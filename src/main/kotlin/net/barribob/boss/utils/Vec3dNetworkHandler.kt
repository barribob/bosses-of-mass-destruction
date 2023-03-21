package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.maelstrom.static_utilities.readVec3d
import net.barribob.maelstrom.static_utilities.writeVec3d
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class Vec3dNetworkHandler {
    private val clientVec3dId = Mod.identifier("client_vec3d")

    fun clientInit() {
        ClientPlayNetworking.registerGlobalReceiver(clientVec3dId) { client, _, buf, _ ->
            handleVec3dPacket(client, buf)
        }
    }

    private fun handleVec3dPacket(client: MinecraftClient, buf: PacketByteBuf) {
        val id = buf.readInt()
        val pos = buf.readVec3d()

        client.execute {
            val world = client.world
            if(world != null) {
                val vecId = VecId.fromInt(id)
                if(vecId != null) vecId.effectHandler().clientHandler(world, pos)
            }
        }
    }

    fun sendVec3dPacket(world: ServerWorld, pos: Vec3d, id: VecId) {
        val buf: PacketByteBuf = PacketByteBufs.create()
        buf.writeInt(id.ordinal)
        buf.writeVec3d(pos)
        for (player in PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
            ServerPlayNetworking.send(player, clientVec3dId, buf)
        }
    }

    companion object {
        fun ServerWorld.sendVec3dPacket(pos: Vec3d, id: VecId){
            Mod.vec3dNetwork.sendVec3dPacket(this, pos, id)
        }
    }
}