package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class ChargedEnderPearlImpactPacket(val pos: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<ChargedEnderPearlImpactPacket> = CustomPayload.Id(Mod.identifier("charged_ender_pearl_impact"))
        val CODEC: PacketCodec<RegistryByteBuf, ChargedEnderPearlImpactPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: ChargedEnderPearlImpactPacket) {
            buf.writeVec3d(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): ChargedEnderPearlImpactPacket {
            val pos = buf.readVec3d()
            return ChargedEnderPearlImpactPacket(pos)
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
