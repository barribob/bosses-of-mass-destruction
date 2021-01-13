package net.barribob.boss.mob.ai.valid_direction

import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestInDesiredRange {

    @Test
    fun isValid_WhenInDesiredRange_ReturnsTrue() {
        val validator = createInDesiredRange({false}, {false}, {true})
        val validator2 = createInDesiredRange({false}, {false}, {false})

        Assertions.assertTrue(validator.isValidDirection(Vec3d.ZERO))
        Assertions.assertTrue(validator2.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun isValid_WhenTooCloseAndMovingAway_ReturnsTrue() {
        val validator = createInDesiredRange({true}, {false}, {false})

        Assertions.assertTrue(validator.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun isValid_WhenTooCloseAndMovingCloser_ReturnsFalse() {
        val validator = createInDesiredRange({true}, {false}, {true})

        Assertions.assertFalse(validator.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun isValid_WhenTooFarAndMovingCloser_ReturnsTrue() {
        val validator = createInDesiredRange({false}, {true}, {true})

        Assertions.assertTrue(validator.isValidDirection(Vec3d.ZERO))
    }

    @Test
    fun isValid_WhenTooFarAndMovingAway_ReturnsFalse() {
        val validator = createInDesiredRange({false}, {true}, {false})

        Assertions.assertFalse(validator.isValidDirection(Vec3d.ZERO))
    }

    private fun createInDesiredRange(
        tooClose: (Vec3d) -> Boolean,
        tooFar: (Vec3d) -> Boolean,
        movingCloser: (Vec3d) -> Boolean
    ) = InDesiredRange(tooClose, tooFar, movingCloser)
}