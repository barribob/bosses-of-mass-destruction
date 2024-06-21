package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class GauntletBlindnessPacket(var id: Int, val players: List<Int>) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<GauntletBlindnessPacket> = CustomPayload.Id(Mod.identifier("gauntlet_blindness"))
        
        val CODEC: PacketCodec<RegistryByteBuf, GauntletBlindnessPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: GauntletBlindnessPacket) {
            buf.writeInt(packet.id)
            buf.writeIntArray(packet.players.toIntArray())
        }
        private fun decode(buf: RegistryByteBuf): GauntletBlindnessPacket {
            return GauntletBlindnessPacket(buf.readInt(), buf.readIntArray().map { i -> i })
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
