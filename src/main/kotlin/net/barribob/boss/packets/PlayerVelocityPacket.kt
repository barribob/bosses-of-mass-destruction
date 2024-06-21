package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class PlayerVelocityPacket(val velocity: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<PlayerVelocityPacket> = CustomPayload.Id(Mod.identifier("player_velocity"))
        val CODEC: PacketCodec<RegistryByteBuf, PlayerVelocityPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: PlayerVelocityPacket) {
            buf.writeVec3d(packet.velocity)
        }
        private fun decode(buf: RegistryByteBuf): PlayerVelocityPacket {
            val pos = buf.readVec3d()
            return PlayerVelocityPacket(pos)
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
