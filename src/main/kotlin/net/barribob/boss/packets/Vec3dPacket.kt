package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class Vec3dPacket(var id: Int, val pos: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<Vec3dPacket> = CustomPayload.Id(Mod.identifier("client_vec3d"))
        val CODEC: PacketCodec<RegistryByteBuf, Vec3dPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: Vec3dPacket) {
            buf.writeInt(packet.id)
            buf.writeVec3d(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): Vec3dPacket {
            return Vec3dPacket(buf.readInt(), buf.readVec3d())
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
