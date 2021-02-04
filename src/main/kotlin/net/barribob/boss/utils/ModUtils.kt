package net.barribob.boss.utils

import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ModUtils {
    /**
     * Look at [ClientPlayNetworkHandler.onParticle]
     */
    fun ServerWorld.spawnParticle(particleType: ParticleEffect, pos: Vec3d, velOrOffset: Vec3d, count: Int = 0) =
        this.spawnParticles(particleType, pos.x, pos.y, pos.z, count, velOrOffset.x, velOrOffset.y, velOrOffset.z, velOrOffset.length())

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