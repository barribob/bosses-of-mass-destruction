package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.entity.Entity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

/**
 * Logic sourced from [MobSpawnerLogic.update]
 */
class SimpleMobSpawner(private val serverWorld: ServerWorld) : IMobSpawner {
    override fun spawn(pos: Vec3d, entity: Entity) {
        entity.setPos(pos)
        if (entity is MobEntity) {
            entity.initialize(serverWorld,
                serverWorld.getLocalDifficulty(entity.getBlockPos()),
                SpawnReason.MOB_SUMMONED,
                null,
                null)
        }

        serverWorld.spawnEntityAndPassengers(entity)

        if (entity is MobEntity) {
            entity.playSpawnEffects()
        }
    }
}