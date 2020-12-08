package net.barribob.invasion.particle

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.particle.ParticleEffect
import net.minecraft.util.math.Vec3d

class ClientParticleBuilder(private val effect: ParticleEffect) {
    private var vel: Vec3d = Vec3d.ZERO
    private var color: ((Float) -> Vec3d)? = null
    private var brightness: ((Float) -> Int)? = null
    private var scale: ((Float) -> Float)? = null
    private var age: (() -> Int)? = null

    fun velocity(velocity: Vec3d): ClientParticleBuilder {
        this.vel = velocity
        return this
    }

    fun color(color: ((Float) -> Vec3d)?): ClientParticleBuilder {
        this.color = color
        return this
    }

    fun brightness(brightness: ((Float) -> Int)?): ClientParticleBuilder {
        this.brightness = brightness
        return this
    }

    fun scale(scale: ((Float) -> Float)?): ClientParticleBuilder {
        this.scale = scale
        return this
    }

    fun age(age: (() -> Int)?): ClientParticleBuilder {
        this.age = age
        return this
    }

    fun build(pos: Vec3d) {
        val client = MinecraftClient.getInstance()
        val camera: Camera = client.gameRenderer.camera
        if (client != null && camera.isReady && client.particleManager != null) {
            val particle = client.particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z)
                    ?: return

            scale?.let { particle.scale(it(0f)) }
            color?.let {
                val clr = it(0f)
                particle.setColor(clr.x.toFloat(), clr.y.toFloat(), clr.z.toFloat())
            }
            age?.let { particle.maxAge = it() }

            if (particle is SimpleParticle) {
                particle.setBrightnessOverride(brightness)
                particle.setColorOverride(color)
                particle.setScaleOverride(scale)
            }
        }
    }
}