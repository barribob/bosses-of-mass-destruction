package net.barribob.invasion.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ModUtils {
    fun ServerWorld.spawnParticle(particleType: ParticleEffect, pos: Vec3d, vel: Vec3d, count: Int = 1) =
        this.spawnParticles(particleType, pos.x, pos.y, pos.z, count, vel.x, vel.y, vel.z, vel.length())

    fun World.playSound(
        pos: Vec3d,
        soundEvent: SoundEvent,
        soundCategory: SoundCategory,
        volume: Float,
        pitch: Float = 1.0f,
        playerEntity: PlayerEntity? = null,
    ) =
        this.playSound(playerEntity, pos.x, pos.y, pos.z, soundEvent, soundCategory, volume, pitch)
}