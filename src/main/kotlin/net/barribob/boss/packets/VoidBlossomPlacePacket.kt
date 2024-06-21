package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class VoidBlossomPlacePacket(val pos: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<VoidBlossomPlacePacket> = CustomPayload.Id(Mod.identifier("void_blossom_place"))
        val CODEC: PacketCodec<RegistryByteBuf, VoidBlossomPlacePacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: VoidBlossomPlacePacket) {
            buf.writeVec3d(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): VoidBlossomPlacePacket {
            return VoidBlossomPlacePacket(buf.readVec3d())
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
