package net.barribob.invasion.particle

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

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    // Todo: unit test this logic
    override fun tick() {
        super.tick()
        if (isAlive) {
            setSpriteForAge(particleContext.spriteProvider)
            val colorOverride1 = colorOverride
            val ageRatio = age / maxAge.toFloat()
            if (colorOverride1 != null) {
                val color = colorOverride1(ageRatio)
                setColor(color.x.toFloat(), color.y.toFloat(), color.z.toFloat())
            }

            val scaleOverride = scaleOverride
            if (scaleOverride != null) {
                scale = scaleOverride(ageRatio)
                setBoundingBoxSpacing(0.2f * scale, 0.2f * scale)
            }
        }
    }

    fun setBrightnessOverride(override: ((Float) -> Int)?) {
        brightnessOverride = override
    }

    fun setColorOverride(override: ((Float) -> Vec3d)?) {
        colorOverride = override
    }

    fun setScaleOverride(override: ((Float) -> Float)?) {
        scaleOverride = override
    }

    override fun getColorMultiplier(tint: Float): Int = brightnessOverride?.invoke(age / maxAge.toFloat()) ?: super.getColorMultiplier(
        tint)

    init {
        this.maxAge = particleAge()
        setSprite(particleContext.spriteProvider)
        velocityX = particleContext.vel.x
        velocityY = particleContext.vel.y
        velocityZ = particleContext.vel.z
    }
}