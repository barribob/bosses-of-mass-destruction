package net.barribob.invasion.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestValidDirectionAnd {
    @Test
    fun validDirection_WhenSomeFalse_IsFalse() {
        val list = listOf(IValidDirection { true }, IValidDirection { false })
        val and = createValidDirection(list)

        Assertions.assertFalse(and.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun validDirection_WhenAllTrue_IsTrue() {
        val list = listOf(IValidDirection { true }, IValidDirection { true })
        val and = createValidDirection(list)

        Assertions.assertTrue(and.isValidDirection(Vec3d.ZERO))
    }

    private fun createValidDirection(list: List<IValidDirection>): ValidDirectionAnd {
        return ValidDirectionAnd(list)
    }
}