package net.barribob.invasion.mob.spawn

import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.MobSpawnerLogic
import net.minecraft.world.WorldView

/**
 * Logic sourced from [MobSpawnerLogic.update] and [MobEntity.canSpawn]
 */
class MobEntitySpawnPredicate(private val worldView: WorldView) : ISpawnPredicate {
    override fun canSpawn(pos: Vec3d, entity: Entity): Boolean {
        return !worldView.containsFluid(entity.boundingBox) &&
                worldView.intersectsEntities(entity) &&
                worldView.isSpaceEmpty(entity.type.createSimpleBoundingBox(pos.x, pos.y, pos.z))
    }
}