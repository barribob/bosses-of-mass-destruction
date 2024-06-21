package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class VoidBlossomHealPacket(val source: Vec3d, val dest : Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<VoidBlossomHealPacket> = CustomPayload.Id(Mod.identifier("void_blossom_heal"))
        val CODEC: PacketCodec<RegistryByteBuf, VoidBlossomHealPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: VoidBlossomHealPacket) {
            buf.writeVec3d(packet.source)
            buf.writeVec3d(packet.dest)
        }
        private fun decode(buf: RegistryByteBuf): VoidBlossomHealPacket {
            return VoidBlossomHealPacket(buf.readVec3d(), buf.readVec3d())
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
