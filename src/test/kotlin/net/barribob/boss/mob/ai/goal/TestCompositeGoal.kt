package net.barribob.boss.mob.ai.goal

import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.minecraft.entity.ai.goal.Goal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class TestCompositeGoal {
    @Test
    fun controls_WhenGoalsHaveMultipleControls_ReturnsUnion() {
        val goal1 = StubGoal(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK))
        val goal2 = StubGoal(EnumSet.of(Goal.Control.TARGET, Goal.Control.LOOK))
        val compositeGoal = CompositeGoal(goal1, goal2)

        Assertions.assertEquals(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.TARGET), compositeGoal.controls)
    }

    class StubGoal(controls: EnumSet<Control>) : Goal() {
        override fun canStart(): Boolean = true

        init {
            this.controls = controls
        }
    }
}