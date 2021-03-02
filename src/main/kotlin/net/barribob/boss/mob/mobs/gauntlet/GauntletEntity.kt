package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class GauntletEntity(entityType: EntityType<out PathAwareEntity>, world: World) : BaseEntity(entityType, world) {
    private val movementHelper = GauntletMovement(this)

    init {
        if(!world.isClient) {
            goalSelector.add(2, CompositeGoal(listOf())) // Idle goal
            goalSelector.add(3, CompositeGoal(listOf(movementHelper.buildAttackMovement(), ActionGoal(::canContinueAttack))))

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))
        }
    }

    private fun canContinueAttack() = isAlive && target != null

    override fun registerControllers(data: AnimationData) {
    }

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?,
    ) {
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this)
    }

    override fun isClimbing(): Boolean = false
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false
    override fun getLookPitchSpeed(): Int = 90
}