package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class GauntletEntity(entityType: EntityType<out PathAwareEntity>, world: World) : BaseEntity(entityType, world),
    MultipartAwareEntity {
    private val movementHelper = GauntletMovement(this)
    private val hitboxHelper = GauntletHitboxes(this)
    override val damageHandler = hitboxHelper

    init {
        if (!world.isClient) {
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

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }

    override fun setPos(x: Double, y: Double, z: Double) {
        super.setPos(x, y, z)
        if(hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun isClimbing(): Boolean = false
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean = false
    override fun getLookPitchSpeed(): Int = 90
    override fun getBoundingBox(): CompoundOrientedBox = hitboxHelper.hitboxes.getBox(super.getBoundingBox())
    override fun getBounds(): EntityBounds = hitboxHelper.hitboxes
    override fun getActiveEyeHeight(pose: EntityPose, dimensions: EntityDimensions) = dimensions.height * 0.4f
    override fun isInsideWall(): Boolean  = false
}
