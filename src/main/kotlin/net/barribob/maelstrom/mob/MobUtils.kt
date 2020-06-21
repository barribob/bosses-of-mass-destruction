package net.barribob.maelstrom.mob

import net.barribob.maelstrom.adapters.GoalConverter
import net.barribob.maelstrom.adapters.IGoal
import net.barribob.maelstrom.general.yOffset
import net.barribob.maelstrom.mob.server.ai.BlockType
import net.minecraft.block.*
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.RevengeGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.boss.dragon.EnderDragonPart
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.MobEntityWithAi
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tag.BlockTags
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import kotlin.math.min
import kotlin.math.pow

/**
 * Static utility functions that use or depend [Entity]
 */
object MobUtils {
    fun leapTowards(entity: LivingEntity, target: Vec3d, horzVel: Double, yVel: Double) {
        val dir = target.subtract(entity.pos).normalize()
        val leap: Vec3d = Vec3d(dir.x, 0.0, dir.z).normalize().multiply(horzVel).yOffset(yVel)
        val clampedYVelocity = if (entity.velocity.y < 0.1) leap.y else 0.0

        // Normalize to make sure the velocity doesn't go beyond what we expect
        var horzVelocity = entity.velocity.add(leap.x, 0.0, leap.z)
        val scale = horzVel / horzVelocity.length()
        if (scale < 1) {
            horzVelocity = horzVelocity.multiply(scale)
        }

        entity.velocity = horzVelocity.yOffset(clampedYVelocity)
    }

    fun handleAreaImpact(radius: Double, maxDamage: Float, source: LivingEntity, pos: Vec3d, damageSource: DamageSource,
                         knockbackFactor: Double = 1.0, fireFactor: Int = 0, damageDecay: Boolean = true, effectCallback: (Entity, Double) -> Unit = { _, _ -> run {} }) {

        val list: List<Entity> = source.world.getEntities(source, Box(BlockPos(pos)).expand(radius))
        val isInstance = { i: Entity -> i is LivingEntity || i is EnderDragonPart || i.collides() }
        val radiusSq = radius.pow(2.0)

        list.stream().filter(isInstance).forEach { entity: Entity ->
            // Get the hitbox size of the entity because otherwise explosions are less
            // effective against larger mobs
            val avgEntitySize: Double = entity.boundingBox.averageSideLength * 0.75

            // Choose the closest distance from the center or the head to encourage
            // headshots
            val distanceToCenter = entity.boundingBox.center.distanceTo(pos)
            val distanceToHead = entity.getCameraPosVec(1.0F).distanceTo(pos)
            val distanceToFeet = entity.pos.distanceTo(pos)
            val distance = min(distanceToCenter, min(distanceToHead, distanceToFeet))

            // Subtracting the average size makes it so that the full damage can be dealt
            // with a direct hit
            val adjustedDistance = (distance - avgEntitySize).coerceAtLeast(0.0)
            val adjustedDistanceSq = adjustedDistance.pow(2.0)
            val damageFactor: Double = if (damageDecay) ((radiusSq - adjustedDistanceSq) / radiusSq).coerceIn(0.0, 1.0) else 1.0

            // Damage decays by the square to make missed impacts less powerful
            val damageFactorSq = damageFactor.pow(2.0)
            val damage: Double = maxDamage * damageFactorSq
            if (damage > 0 && adjustedDistanceSq < radiusSq) {
                entity.setOnFireFor((fireFactor * damageFactorSq).toInt())
                entity.damage(damageSource, damage.toFloat())
                val entitySizeFactor: Double = if (avgEntitySize == 0.0) 1.0 else (1.0 / avgEntitySize).coerceIn(0.5, 1.0)
                val entitySizeFactorSq = entitySizeFactor.pow(2.0)

                // Velocity depends on the entity's size and the damage dealt squared
                val velocity: Vec3d = entity.boundingBox.center.subtract(pos).normalize().multiply(damageFactorSq).multiply(knockbackFactor).multiply(entitySizeFactorSq)
                entity.addVelocity(velocity.x, velocity.y, velocity.z)
                effectCallback(entity, damageFactorSq)
            }
        }
    }

    fun isEntityInWorld(entity: Entity): Boolean {
        return entity.world.getEntityById(entity.entityId) == null
    }

    fun getSwimmingGoal(priority: Int, entity: MobEntity) : Pair<Int, IGoal> {
        return Pair(priority, GoalConverter(SwimGoal(entity)))
    }

    fun getWanderingGoal(priority: Int, distance: Double, entity: MobEntityWithAi) : Pair<Int, IGoal> {
        return Pair(priority, GoalConverter(WanderAroundFarGoal(entity, distance)))
    }

    fun getTargetSelectGoal(
            priority: Int,
            entity: MobEntityWithAi,
            targetOnlyPlayers: Boolean = false,
            checkVisibility: Boolean = true,
            checkNavigation: Boolean = false,
            chance: Int = 10, condition:
            (LivingEntity) -> Boolean = { true }) : Pair<Int, IGoal> {
        return if(targetOnlyPlayers) {
            Pair(priority, GoalConverter(FollowTargetGoal(entity, PlayerEntity::class.java, chance, checkVisibility, checkNavigation) { condition(it) }))
        }
        else {
            Pair(priority, GoalConverter(FollowTargetGoal(entity, LivingEntity::class.java, chance, checkVisibility, checkNavigation) { condition(it) }))
        }
    }

    fun getRevengeGoal(priority: Int, entity: MobEntityWithAi) : Pair<Int, IGoal> {
        return Pair(priority, GoalConverter(RevengeGoal(entity, *arrayOfNulls(0))))
    }

    fun getBlockType(world: BlockView, pos: BlockPos, callsLeft: Int): BlockType {
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        val material = blockState.material
        val fluidState = world.getFluidState(pos)
        val belowType = if(pos.y > 0 && callsLeft > 0) getBlockType(world, pos.down(), callsLeft - 1) else BlockType.OPEN

        return when {
            blockState.isOf(Blocks.SWEET_BERRY_BUSH) ||
                    blockState.isIn(BlockTags.FIRE) ||
                    CampfireBlock.isLitCampfire(blockState) ||
                    fluidState.matches(FluidTags.WATER) -> BlockType.PASSABLE_OBSTACLE
            fluidState.matches(FluidTags.LAVA) ||
                    blockState.isOf(Blocks.CACTUS) ||
                    blockState.isOf(Blocks.HONEY_BLOCK) ||
                    blockState.isOf(Blocks.MAGMA_BLOCK) -> BlockType.SOLID_OBSTACLE
            block is LeavesBlock ||
                    block.isIn(BlockTags.FENCES) ||
                    block.isIn(BlockTags.WALLS) ||
                    (block is FenceGateBlock && !blockState.get(FenceGateBlock.OPEN)) ||
                    (DoorBlock.isWoodenDoor(blockState) && !blockState.get(DoorBlock.OPEN)) ||
                    (block is DoorBlock && material == Material.METAL && !blockState.get(DoorBlock.OPEN)) ||
                    (block is DoorBlock && blockState.get(DoorBlock.OPEN)) ||
                    !blockState.canPathfindThrough(world, pos, NavigationType.LAND) -> BlockType.BLOCKED
            belowType == BlockType.BLOCKED -> BlockType.WALKABLE
            belowType == BlockType.OPEN -> BlockType.PASSABLE_OBSTACLE
            belowType == BlockType.PASSABLE_OBSTACLE -> BlockType.PASSABLE_OBSTACLE
            belowType == BlockType.SOLID_OBSTACLE -> BlockType.PASSABLE_OBSTACLE
            else -> BlockType.OPEN
        }
    }
}