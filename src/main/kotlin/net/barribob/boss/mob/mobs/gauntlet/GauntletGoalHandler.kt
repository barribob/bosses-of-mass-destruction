package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IMoveHandler
import net.barribob.boss.mob.utils.INbtHandler
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.entity.MovementType
import net.minecraft.entity.ai.goal.GoalSelector
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Vec3d

class GauntletGoalHandler(
    val entity: GauntletEntity,
    private val goalSelector: GoalSelector,
    private val targetSelector: GoalSelector,
    eventScheduler: EventScheduler,
    mobConfig: GauntletConfig
) : INbtHandler, IMoveHandler, IDamageHandler {
    private var isAggroed = false
    private val movementHelper = GauntletMovement(entity)
    private val attackHelper = GauntletAttacks(entity, eventScheduler, mobConfig)

    private fun addGoals() {
        goalSelector.add(2, CompositeGoal(listOf())) // Idle goal
        goalSelector.add(3, CompositeGoal(listOf(movementHelper.buildAttackMovement(), attackHelper.buildAttackGoal())))

        targetSelector.add(2, FindTargetGoal(entity, PlayerEntity::class.java, { entity.boundingBox.expand(it) }))
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putBoolean(::isAggroed.name, isAggroed)
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        if (tag.contains(::isAggroed.name)) {
            isAggroed = tag.getBoolean(::isAggroed.name)
            if (isAggroed) addGoals()
        }
    }

    override fun canMove(type: MovementType, movement: Vec3d): Boolean = isAggroed

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        if (result && !isAggroed) {
            isAggroed = true
            addGoals()
        }
    }
}