package net.barribob.boss.utils

import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.block.SideShapeType
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.*
import net.minecraft.world.Difficulty
import net.minecraft.world.World
import java.util.*

object ModUtils {
    /**
     * Look at [ClientPlayNetworkHandler.onParticle]
     */
    fun ServerWorld.spawnParticle(particleType: ParticleEffect, pos: Vec3d, velOrOffset: Vec3d, count: Int = 0, speed: Double = 0.0) =
        this.spawnParticles(
            particleType,
            pos.x,
            pos.y,
            pos.z,
            count,
            velOrOffset.x,
            velOrOffset.y,
            velOrOffset.z,
            speed
        )

    fun ServerWorld.playSound(
        pos: Vec3d,
        soundEvent: SoundEvent,
        soundCategory: SoundCategory,
        volume: Float,
        pitch: Float = this.random.randomPitch(),
        range: Double = if (volume > 1.0f) (16.0f * volume).toDouble() else 16.0,
        playerEntity: PlayerEntity? = null,
    ) =
        this.server.playerManager.sendToAround(
            playerEntity,
            pos.x,
            pos.y,
            pos.z,
            range,
            registryKey,
            PlaySoundS2CPacket(soundEvent, soundCategory, pos.x, pos.y, pos.z, volume, pitch)
        )

    fun Random.randomPitch() = (this.nextFloat() - this.nextFloat()) * 0.2f + 1.0f

    fun World.findGroundBelow(pos: BlockPos, isOpenBlock: (BlockPos) -> Boolean = { true }): BlockPos {
        for (i in pos.y downTo this.bottomY + 1) {
            val tempPos = BlockPos(pos.x, i, pos.z)
            if (this.getBlockState(tempPos).isSideSolid(this, tempPos, Direction.UP, SideShapeType.FULL) && isOpenBlock(tempPos.up())) {
                return tempPos
            }
        }
        return BlockPos(pos.x, this.bottomY, pos.z)
    }

    fun preventDespawnExceptPeaceful(entity: MobEntity, world: World) {
        if (world.difficulty == Difficulty.PEACEFUL) {
            entity.discard()
        } else {
            entity.despawnCounter = 0
        }
    }

    /**
     * From Maelstrom Mod ModUtils.java
     */
    fun World.findEntitiesInLine(start: Vec3d, end: Vec3d, toExclude: Entity?): List<Entity> =
        this.getOtherEntities(toExclude, Box(start, end)) { it.boundingBox.raycast(start, end).isPresent }

    val ServerPlayerEntity.serverWorld: ServerWorld
        get() = this.getWorld()

    val MatrixStack.Entry.normal: Matrix3f
        get() = this.normalMatrix

    val MatrixStack.Entry.model: Matrix4f
        get() = this.positionMatrix

    data class RotatingParticles(val pos: Vec3d, val particleBuilder: ClientParticleBuilder, val minRadius: Double, val maxRadius: Double, val minSpeed: Double, val maxSpeed: Double)

    fun spawnRotatingParticles(particleParams: RotatingParticles) {
            val startingRotation = kotlin.random.Random.nextInt(360)
            val randomRadius = RandomUtils.range(particleParams.minRadius, particleParams.maxRadius)
            val rotationSpeed = RandomUtils.range(particleParams.minSpeed, particleParams.maxSpeed)
            particleParams.particleBuilder
                .continuousPosition { rotateAroundPos(particleParams.pos, it.getAge(), startingRotation, randomRadius, rotationSpeed) }
                .build(rotateAroundPos(particleParams.pos, 0, startingRotation, randomRadius, rotationSpeed), Vec3d.ZERO)
    }

    private fun rotateAroundPos(
        pos: Vec3d,
        age: Int,
        startingRotation: Int,
        radius: Double,
        rotationSpeed: Double
    ): Vec3d {
        val xzOffset = VecUtils.xAxis.rotateY(Math.toRadians(age * rotationSpeed + startingRotation).toFloat())
        return pos.add(xzOffset.multiply(radius))
    }
}