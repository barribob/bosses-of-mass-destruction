package net.barribob.invasion.mob.ai

import net.barribob.maelstrom.static_utilities.addVelocity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.PathAwareEntity
import java.util.*

class AerialWanderGoal(
    private val mob: PathAwareEntity,
    private val steering: ISteering,
    private val targetSelector: ITargetSelector
) : Goal() {
    init {
        controls = EnumSet.of(Control.MOVE)
    }

    override fun canStart(): Boolean = true

    override fun tick() {
        val target = targetSelector.getTarget()
        val velocity = steering.accelerateTo(target)
        mob.addVelocity(velocity)

        super.tick()
    }
}