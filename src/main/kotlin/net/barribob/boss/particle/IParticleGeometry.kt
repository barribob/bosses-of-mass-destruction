package net.barribob.boss.particle

import net.minecraft.client.render.Camera
import net.minecraft.client.util.math.Vector3f

fun interface IParticleGeometry {
    fun getGeometry(
        camera: Camera,
        tickDelta: Float,
        prevPosX: Double,
        prevPosY: Double,
        prevPosZ: Double,
        x: Double,
        y: Double,
        z: Double,
        scale: Float
    ): Array<Vector3f>
}