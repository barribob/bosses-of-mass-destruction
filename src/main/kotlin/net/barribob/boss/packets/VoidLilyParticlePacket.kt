package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class VoidLilyParticlePacket(val pos: Vec3d, val dir : Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<VoidLilyParticlePacket> = CustomPayload.Id(Mod.identifier("void_lily_pollen"))
        val CODEC: PacketCodec<RegistryByteBuf, VoidLilyParticlePacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: VoidLilyParticlePacket) {
            buf.writeVec3d(packet.pos)
            buf.writeVec3d(packet.dir)
        }
        private fun decode(buf: RegistryByteBuf): VoidLilyParticlePacket {
            return VoidLilyParticlePacket(buf.readVec3d(), buf.readVec3d())
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
