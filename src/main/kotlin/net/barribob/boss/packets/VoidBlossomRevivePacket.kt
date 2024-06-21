package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class VoidBlossomRevivePacket(val pos: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<VoidBlossomRevivePacket> = CustomPayload.Id(Mod.identifier("void_blossom_revive"))
        val CODEC: PacketCodec<RegistryByteBuf, VoidBlossomRevivePacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: VoidBlossomRevivePacket) {
            buf.writeVec3d(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): VoidBlossomRevivePacket {
            val pos = buf.readVec3d()
            return VoidBlossomRevivePacket(pos)
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
