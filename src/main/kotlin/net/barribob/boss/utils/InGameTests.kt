package net.barribob.boss.utils

import net.barribob.boss.Mod
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.ClientServerUtils
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

object InGameTests {
    fun throwProjectile(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val projectile = MagicMissileProjectile(entity, entity.world, {}, listOf(EntityType.ZOMBIE))
            projectile.setProperties(entity, entity.pitch, entity.yaw, 0f, 1.5f, 1.0f)
            entity.world.spawnEntity(projectile)
        }
    }

    fun axisOffset(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val points = mutableListOf<Vec3d>()
            for (vec in listOf(VecUtils.xAxis, VecUtils.yAxis, VecUtils.zAxis)) {
                val offset = MathUtils.axisOffset(entity.rotationVector, vec)
                val pos = entity.getCameraPosVec(0f)
                MathUtils.lineCallback(pos, offset.add(pos), 30) { v, _ -> points.add(v) }
            }
            ClientServerUtils.drawDebugPoints(points, 1, entity.pos, entity.world)
        }
    }

    fun spawnEntity(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        val serverWorld = entity.world as ServerWorld
        val compoundTag = CompoundTag()
        compoundTag.putString("id", Registry.ENTITY_TYPE.getId(EntityType.PHANTOM).toString())

        val spawner = MobPlacementLogic(
            RangedSpawnPosition({ entity.pos }, 3.0, 6.0, ModRandom()),
            CompoundTagEntityProvider(compoundTag, serverWorld, Mod.LOGGER),
            MobEntitySpawnPredicate(entity.world),
            SimpleMobSpawner(serverWorld)
        )
        spawner.tryPlacement(10)
    }

    fun testClient(source: ServerCommandSource) {
        NetworkUtils.testClient(source.world, source.position)
    }

    fun testClientCallback(packetContext: PacketContext) {
        val deathParticleFactory = ParticleFactories.soulFlame()
            .color { MathUtils.lerpVec(it, ModColors.COMET_BLUE, ModColors.FADED_COMET_BLUE) }
            .age { RandomUtils.range(40, 80) }
            .velocity { RandomUtils.randVec() }
            .colorVariation(0.5)
            .scale { 0.5f - (it * 0.3f) }

        MaelstromMod.clientEventScheduler.addEvent(TimedEvent({
            for(i in 0..4) {
                deathParticleFactory.build(packetContext.player.pos)
            }
        }, 0, 10))
    }
}