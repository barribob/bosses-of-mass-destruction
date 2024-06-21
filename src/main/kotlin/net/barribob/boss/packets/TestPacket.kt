package net.barribob.boss.packets

import net.barribob.boss.Mod
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class TestPacket : CustomPayload {
    companion object {
        val ID: CustomPayload.Id<TestPacket> = CustomPayload.Id(Mod.identifier("client_test"))
        val codec: PacketCodec<RegistryByteBuf, TestPacket> = PacketCodec.of({ _, _ -> }, { _ -> TestPacket() })
    }
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
