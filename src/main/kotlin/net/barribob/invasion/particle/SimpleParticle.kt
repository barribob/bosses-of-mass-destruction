package net.barribob.invasion.particle

import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle

class SimpleParticle(private val particleContext: ParticleContext, particleAge: () -> Int) :
    SpriteBillboardParticle(
        particleContext.world,
        particleContext.pos.x,
        particleContext.pos.y,
        particleContext.pos.z
    ) {

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun tick() {
        super.tick()
        if (isAlive) {
            setSpriteForAge(particleContext.spriteProvider)
        }
    }

    init {
        this.maxAge = particleAge()
        setSprite(particleContext.spriteProvider)
        velocityX = particleContext.vel.x
        velocityY = particleContext.vel.y
        velocityZ = particleContext.vel.z
    }
}