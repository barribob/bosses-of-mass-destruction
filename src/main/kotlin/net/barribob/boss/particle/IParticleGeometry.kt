package net.barribob.boss.particle

import net.minecraft.client.render.Camera
import org.joml.Vector3f

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
        scale: Float,
        rotation: Float
    ): Array<Vector3f>
}