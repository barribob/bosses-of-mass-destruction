package net.barribob.boss.mob.spawn

import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.MobSpawnerLogic
import net.minecraft.world.SpawnHelper
import net.minecraft.world.WorldView

/**
 * Logic sourced from [MobSpawnerLogic.update] and [MobEntity.canSpawn]
 */
class MobEntitySpawnPredicate(private val worldView: WorldView) : ISpawnPredicate {
    override fun canSpawn(pos: Vec3d, entity: Entity): Boolean {
        val blockPos = BlockPos(pos)
        val blockState = worldView.getBlockState(blockPos)
        val fluidState = worldView.getFluidState(blockPos)
        entity.updatePosition(pos.x, pos.y, pos.z)

        return !worldView.containsFluid(entity.boundingBox) &&
                worldView.intersectsEntities(entity) &&
                worldView.isSpaceEmpty(entity.type.createSimpleBoundingBox(pos.x, pos.y, pos.z))
                && SpawnHelper.isClearForSpawn(worldView, blockPos, blockState, fluidState, entity.type)
    }
}