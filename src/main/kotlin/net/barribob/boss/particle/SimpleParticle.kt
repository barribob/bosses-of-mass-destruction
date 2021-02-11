package net.barribob.boss.particle

import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.coerceAtLeast
import net.barribob.maelstrom.static_utilities.coerceAtMost
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.util.math.Vec3d

class SimpleParticle(
    private val particleContext: ParticleContext,
    particleAge: () -> Int,
    private val particleGeometry: IParticleGeometry,
    private val cycleSprites: Boolean = true
) :
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
    private var velocityOverride: ((SimpleParticle) -> Vec3d)? = null

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    fun getPos(): Vec3d{
        return Vec3d(x, y, z)
    }

    override fun tick() {
        super.tick()
        if (isAlive) {
            if (cycleSprites) setSpriteForAge(particleContext.spriteProvider)
            val ageRatio = age / maxAge.toFloat()
            setColorFromOverride(colorOverride, ageRatio)
            setScaleFromOverride(scaleOverride, ageRatio)
            setVelocityFromOverride(velocityOverride)
        }
    }

    private fun setVelocityFromOverride(velocityOverride: ((SimpleParticle) -> Vec3d)?) {
        if (velocityOverride != null) {
            val velocity = velocityOverride(this)
            velocityX = velocity.x
            velocityY = velocity.y
            velocityZ = velocity.z
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

    fun setVelocityOverride(override: ((SimpleParticle) -> Vec3d)?) {
        velocityOverride = override
    }

    override fun getColorMultiplier(tint: Float): Int =
        brightnessOverride?.invoke(age / maxAge.toFloat()) ?: super.getColorMultiplier(tint)

    override fun buildGeometry(vertexConsumer: VertexConsumer?, camera: Camera, tickDelta: Float) {
        val vector3fs = particleGeometry.getGeometry(
            camera,
            tickDelta,
            prevPosX, prevPosY, prevPosZ,
            x, y, z,
            getSize(tickDelta)
        )

        val l = this.minU
        val m = this.maxU
        val n = this.minV
        val o = this.maxV
        val p = getColorMultiplier(tickDelta)
        vertexConsumer!!.vertex(
            vector3fs[0].x.toDouble(), vector3fs[0].y.toDouble(),
            vector3fs[0].z.toDouble()
        ).texture(m, o).color(colorRed, colorGreen, colorBlue, colorAlpha).light(p).next()
        vertexConsumer.vertex(
            vector3fs[1].x.toDouble(), vector3fs[1].y.toDouble(),
            vector3fs[1].z.toDouble()
        ).texture(m, n).color(colorRed, colorGreen, colorBlue, colorAlpha).light(p).next()
        vertexConsumer.vertex(
            vector3fs[2].x.toDouble(), vector3fs[2].y.toDouble(),
            vector3fs[2].z.toDouble()
        ).texture(l, n).color(colorRed, colorGreen, colorBlue, colorAlpha).light(p).next()
        vertexConsumer.vertex(
            vector3fs[3].x.toDouble(), vector3fs[3].y.toDouble(),
            vector3fs[3].z.toDouble()
        ).texture(l, o).color(colorRed, colorGreen, colorBlue, colorAlpha).light(p).next()
    }

    init {
        this.maxAge = particleAge()
        if (cycleSprites) setSpriteForAge(particleContext.spriteProvider) else setSprite(particleContext.spriteProvider)
        velocityX = particleContext.vel.x
        velocityY = particleContext.vel.y
        velocityZ = particleContext.vel.z
    }
}