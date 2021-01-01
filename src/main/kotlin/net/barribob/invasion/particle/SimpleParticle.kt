package net.barribob.invasion.particle

import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.coerceAtLeast
import net.barribob.maelstrom.static_utilities.coerceAtMost
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.util.math.Vec3d

class SimpleParticle(private val particleContext: ParticleContext, particleAge: () -> Int) :
    SpriteBillboardParticle(
        particleContext.world,
        particleContext.pos.x,
        particleContext.pos.y,
        particleContext.pos.z
    ) {

    private var brightnessOverride: ((Float) -> Int)? = null
    private var colorOverride: ((Float) -> Vec3d)? = null
    private var scaleOverride: ((Float) -> Float)? = null
    private var colorVariation: Vec3d = Vec3d.ZERO

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    // Todo: unit test this logic
    override fun tick() {
        super.tick()
        if (isAlive) {
            setSpriteForAge(particleContext.spriteProvider)
            val ageRatio = age / maxAge.toFloat()
            setColorFromOverride(colorOverride, ageRatio)
            setScaleFromOverride(scaleOverride, ageRatio)
        }
    }

    private fun setScaleFromOverride(scaleOverride: ((Float) -> Float)?, ageRatio: Float) {
        if (scaleOverride != null) {
            scale = scaleOverride(ageRatio)
            setBoundingBoxSpacing(0.2f * scale, 0.2f * scale)
        }
    }

    private fun setColorFromOverride(colorOverride: ((Float) -> Vec3d)?, ageRatio: Float) {
        if (colorOverride != null) {
            val color = colorOverride(ageRatio)
            val variedColor = color.add(colorVariation).coerceAtLeast(Vec3d.ZERO).coerceAtMost(VecUtils.unit)
            setColor(variedColor.x.toFloat(), variedColor.y.toFloat(), variedColor.z.toFloat())
        }
    }

    fun setBrightnessOverride(override: ((Float) -> Int)?) {
        brightnessOverride = override
    }

    fun setColorOverride(override: ((Float) -> Vec3d)?) {
        colorOverride = override
        setColorFromOverride(override, 0f)
    }

    fun setScaleOverride(override: ((Float) -> Float)?) {
        scaleOverride = override
        setScaleFromOverride(override, 0f)
    }

    fun setColorVariation(variation: Double) {
        colorVariation = RandomUtils.randVec().multiply(variation)
        setColorFromOverride(colorOverride, 0f)
    }

    override fun getColorMultiplier(tint: Float): Int =
        brightnessOverride?.invoke(age / maxAge.toFloat()) ?: super.getColorMultiplier(tint)

    init {
        this.maxAge = particleAge()
        setSpriteForAge(particleContext.spriteProvider)
        velocityX = particleContext.vel.x
        velocityY = particleContext.vel.y
        velocityZ = particleContext.vel.z
    }
}