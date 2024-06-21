package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class GauntletChangeHitboxPacket(val entityId: Int, val open: Boolean) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<GauntletChangeHitboxPacket> = CustomPayload.Id(Mod.identifier("change_hitbox"))
        val CODEC: PacketCodec<RegistryByteBuf, GauntletChangeHitboxPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: GauntletChangeHitboxPacket) {
            buf.writeInt(packet.entityId);
            buf.writeBoolean(packet.open);
        }
        private fun decode(buf: RegistryByteBuf): GauntletChangeHitboxPacket {
            return GauntletChangeHitboxPacket(buf.readInt(), buf.readBoolean());
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
