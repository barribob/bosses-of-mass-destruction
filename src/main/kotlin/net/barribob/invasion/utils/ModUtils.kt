package net.barribob.invasion.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
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
        range: Double = if (volume > 1.0f) (16.0f * volume).toDouble() else 16.0,
        playerEntity: PlayerEntity? = null,
    ) =
        this.server!!.playerManager.sendToAround(playerEntity,
            pos.x,
            pos.y,
            pos.z,
            range,
            registryKey,
            PlaySoundS2CPacket(soundEvent, soundCategory, pos.x, pos.y, pos.z, volume, pitch))
}