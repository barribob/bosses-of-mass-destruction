package net.barribob.invasion.utils

import io.netty.buffer.Unpooled
import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder

object ModUtils {
    val SPAWN_ENTITY_PACKET_ID = Invasions.identifier("spawn_entity")

    fun ServerWorld.spawnParticle(particleType: ParticleEffect, pos: Vec3d, vel: Vec3d, count: Int = 1) =
        this.spawnParticles(particleType, pos.x, pos.y, pos.z, count, vel.x, vel.y, vel.z, vel.length())

    fun World.playSound(
        pos: Vec3d,
        soundEvent: SoundEvent,
        soundCategory: SoundCategory,
        volume: Float,
        pitch: Float = 1.0f,
        playerEntity: PlayerEntity? = null
    ) =
        this.playSound(playerEntity, pos.x, pos.y, pos.z, soundEvent, soundCategory, volume, pitch)

    private fun packSpawnClientEntity(packet: EntitySpawnS2CPacket): PacketByteBuf {
        val packetData = PacketByteBuf(Unpooled.buffer())
        packet.write(packetData)
        return packetData
    }

    // Todo: probably temporary since they completely limit what information we can send to the client
    fun createClientEntityPacket(entity: Entity): Packet<*> {
        return ServerSidePacketRegistry.INSTANCE.toPacket(SPAWN_ENTITY_PACKET_ID, packSpawnClientEntity(
            EntitySpawnS2CPacket(entity)))
    }

    fun handleSpawnClientEntity(packetContext: PacketContext, buf: PacketByteBuf) {
        val packet = EntitySpawnS2CPacket()
        packet.read(buf)
        packetContext.taskQueue.execute { VanillaCopies.handleClientSpawnEntity(packetContext, packet) }
    }

    fun <T : IAnimatable> createIdlePredicate(animationName: String): AnimationPredicate<T> = AnimationPredicate {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation(animationName, true)
        )
        PlayState.CONTINUE
    }
}