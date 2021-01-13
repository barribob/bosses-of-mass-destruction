package net.barribob.boss.mob.ai.goals

import net.barribob.boss.mob.ai.ISteering
import net.barribob.boss.mob.ai.ITargetSelector
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.util.math.Vec3d
import java.util.*

class VelocityGoal(
    private val onTargetSelected: (Vec3d) -> Unit,
    private val steering: ISteering,
    private val targetSelector: ITargetSelector
) : Goal() {
    init {
        controls = EnumSet.of(Control.MOVE, Control.LOOK)
    }

    override fun canStart(): Boolean = true

    override fun tick() {
        val target = targetSelector.getTarget()
        val velocity = steering.accelerateTo(target)
        onTargetSelected(velocity)

        super.tick()
    }
}