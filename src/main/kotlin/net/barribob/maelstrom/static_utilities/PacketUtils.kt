package net.barribob.maelstrom.static_utilities

import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

fun PacketByteBuf.readFloatList(count: Int): List<Float> {
    if (count < 0) throw IllegalArgumentException("Count should be greater than zero")
    return (0 until count).map { this.readFloat() }
}

fun PacketByteBuf.writeFloatList(list: List<Float>) = list.forEach { this.writeFloat(it) }

fun PacketByteBuf.writeVec3d(vec: Vec3d) = this.writeFloatList(
    listOf(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat()))

fun PacketByteBuf.readVec3d(): Vec3d {
    val floatList = readFloatList(3)
    return Vec3d(floatList[0].toDouble(), floatList[1].toDouble(), floatList[2].toDouble())
}