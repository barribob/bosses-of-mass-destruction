package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

data class ObsidilithRevivePacket(val pos: Vec3d) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<ObsidilithRevivePacket> = CustomPayload.Id(Mod.identifier("obsidilith_revive"))
        val CODEC: PacketCodec<RegistryByteBuf, ObsidilithRevivePacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: ObsidilithRevivePacket) {
            buf.writeVec3d(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): ObsidilithRevivePacket {
            val pos = buf.readVec3d()
            return ObsidilithRevivePacket(pos)
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
