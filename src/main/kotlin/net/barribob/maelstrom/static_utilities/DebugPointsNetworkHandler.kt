package net.barribob.maelstrom.static_utilities

import io.netty.buffer.Unpooled
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.render.RenderData
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class DebugPointsNetworkHandler {
    fun drawDebugPoints(points: List<Vec3d>, time: Int, watchPoint: Vec3d, world: ServerWorld, color: List<Float> = listOf(1f, 1f, 1f, 1f)) {
        val packetData = packDrawDebugPoints(time, color, points)

        PlayerLookup.around(world, watchPoint, 100.0).forEach {
            ServerPlayNetworking.send(it, MaelstromMod.DRAW_POINTS_PACKET_ID, packetData)
        }
    }

    fun packDrawDebugPoints(time: Int, color: List<Float>, points: List<Vec3d>): PacketByteBuf {
        if (color.size != 4) throw IllegalArgumentException("Color needs to be defined with 4 floats")
        val packetData = PacketByteBuf(Unpooled.buffer())
        packetData.writeInt(time)
        packetData.writeInt(points.size)
        packetData.writeFloatList(color)
        packetData.writeFloatList(points.flatMap { listOf(it.x, it.y, it.z) }.map(Double::toFloat))
        return packetData
    }

    @Environment(EnvType.CLIENT)
    fun unpackDrawDebugPoints(packetData: PacketByteBuf): Triple<Int, List<Float>, List<Float>> {
        val delay = packetData.readInt()
        val numPoints = packetData.readInt()
        val colorArray = packetData.readFloatList(4)
        val pointArray = packetData.readFloatList(numPoints * 3)
        return Triple(delay, colorArray, pointArray)
    }

    @Environment(EnvType.CLIENT)
    fun drawDebugPointsClient(client: MinecraftClient, packetData: PacketByteBuf) {
        val (delay, colorArray, pointArray) = unpackDrawDebugPoints(packetData)

        client.execute {
            val renderer = { renderData: RenderData -> RenderUtils.renderPoints(renderData, colorArray, pointArray) }
            val removeEvent: () -> Unit = { MaelstromMod.renderMap.renderMap.remove(renderer) }

            MaelstromMod.renderMap.renderMap.add(renderer)
            MaelstromMod.clientEventScheduler.addEvent(TimedEvent(removeEvent, delay))
        }
    }
}