package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class VoidBlossomSpikesPacket(val id: Int, val pos: BlockPos) : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<VoidBlossomSpikesPacket> = CustomPayload.Id(Mod.identifier("void_blossom_spikes"))
        val CODEC: PacketCodec<RegistryByteBuf, VoidBlossomSpikesPacket> = PacketCodec.ofStatic(::encode, ::decode);

        private fun encode(buf: RegistryByteBuf, packet: VoidBlossomSpikesPacket) {
            buf.writeInt(packet.id)
            buf.writeBlockPos(packet.pos)
        }
        private fun decode(buf: RegistryByteBuf): VoidBlossomSpikesPacket {
            return VoidBlossomSpikesPacket(buf.readInt(), buf.readBlockPos())
        }
    }
    
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
