package net.barribob.maelstrom.animation.server

import io.netty.buffer.Unpooled
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.animation.client.AnimationLoader
import net.barribob.maelstrom.mob.MobUtils
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import java.util.*
import java.util.stream.Stream

/**
 * Purpose of a server animation manager is to make sure client doesn't "lose" looping animations over time. I don't with normal animations because I don't expect them to be longer than a few seconds.
 *
 * @author Barribob
 *
 */
class ServerAnimationWatcher {
    private val animationLoader = AnimationLoader()
    private val loopingAnimations = HashMap<LivingEntity, MutableMap<String, Int>>()
    private val refreshRate = 20

    /**
     * Animation id of the format: animation_filename.animation_name. For example if I have an animation file called "anim.json" and inside it there is one animation under the "animations" object named
     * "walk", then the id would be "anim.walk"
     */
    fun startAnimation(entity: LivingEntity, animationId: String) {
        val watchingPlayers: Stream<PlayerEntity> = PlayerStream.watching(entity)
        val packetData = PacketByteBuf(Unpooled.buffer())
        packetData.writeInt(entity.entityId)
        packetData.writeString(animationId)
        watchingPlayers.forEach { ServerSidePacketRegistry.INSTANCE.sendToPlayer(it, MaelstromMod.START_ANIMATION_PACKET_ID, packetData) }

        // TODO: The server needs to watch looping animations
    }

    fun stopAnimation(entity: LivingEntity, animationId: String) {
        throw NotImplementedError()
    }

    fun appendAnimationTo(entity: LivingEntity, animationId: String, existinganimation: String) {
        throw NotImplementedError()
    }

    /**
     * The server periodically will update the clients with any looping animations it finds to solve the problem with failing looping animations as mentioned above
     */
    fun tick() {
        val entitiesToRemove: MutableList<LivingEntity> = ArrayList<LivingEntity>()
        for ((entity, value) in loopingAnimations) {
            // TODO: make sure that this second condition actually works
            if (!entity.isAlive && MobUtils.isEntityInWorld(entity)) {
                entitiesToRemove.add(entity)
                continue
            }
            for (kv in value.entries) {
                if (kv.value % refreshRate == 0) {
                    // TODO: implement looping animation updates
//                    Main.network.sendToAllTracking(MessageLoopAnimationUpdate(ModBBAnimations.getAnimationId(kv.key), entity.entityId), entity)
                }
                kv.setValue(kv.value + 1)
            }
        }

        // Remove entities not in this world anymore
        for (entity in entitiesToRemove) {
            loopingAnimations.remove(entity)
        }
    }
}