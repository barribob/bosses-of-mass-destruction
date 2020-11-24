package net.barribob.invasion.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestValidDirectionOr {
    @Test
    fun validDirection_WhenSomeTrue_IsTrue() {
        val list = listOf(IValidDirection { true }, IValidDirection { false })
        val and = createValidDirection(list)

        Assertions.assertTrue(and.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun validDirection_WhenAllFalse_IsFalse() {
        val list = listOf(IValidDirection { false }, IValidDirection { false })
        val and = createValidDirection(list)

        Assertions.assertFalse(and.isValidDirection(Vec3d.ZERO))
    }

    private fun createValidDirection(list: List<IValidDirection>): ValidDirectionOr {
        return ValidDirectionOr(list)
    }
}