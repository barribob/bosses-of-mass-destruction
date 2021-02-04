package net.barribob.boss.particle

import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d

data class ParticleContext (val spriteProvider: SpriteProvider, val world: ClientWorld, val pos: Vec3d, val vel: Vec3d, val cycleSprites: Boolean = true)