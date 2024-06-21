package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.packets.Vec3dPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class Vec3dNetworkHandler {

    fun clientInit() {
        ClientPlayNetworking.registerGlobalReceiver(Vec3dPacket.ID) { packet, context ->
            handleVec3dPacket(context.client(), packet)
        }
    }


    fun registerHandlers() {
        PayloadTypeRegistry.playS2C().register(Vec3dPacket.ID, Vec3dPacket.CODEC);
    }

    private fun handleVec3dPacket(client: MinecraftClient, packet: Vec3dPacket) {
        val id = packet.id
        val pos = packet.pos

        client.execute {
            val world = client.world
            if(world != null) {
                val vecId = VecId.fromInt(id)
                if(vecId != null) vecId.effectHandler().clientHandler(world, pos)
            }
        }
    }

    fun sendVec3dPacket(world: ServerWorld, pos: Vec3d, id: VecId) {
        val packet = Vec3dPacket(id.ordinal, pos)
        for (player in PlayerLookup.tracking(world, BlockPos.ofFloored(pos))) {
            ServerPlayNetworking.send(player, packet)
        }
    }

    companion object {
        fun ServerWorld.sendVec3dPacket(pos: Vec3d, id: VecId){
            Mod.vec3dNetwork.sendVec3dPacket(this, pos, id)
        }
    }
}