package net.barribob.invasion.particle

import net.barribob.invasion.render.FrameLimiter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestFrameLimiter {

    @Test
    fun canDoFrame_WhenFirstCall_AlwaysReturnTrue() {
        val limiter = createFrameLimiter()

        Assertions.assertTrue(limiter.canDoFrame(0.1f))
    }

    @Test
    fun canDoFrame_WhenFrameDeltaTooSmall_ReturnFalse() {
        val limiter = createFrameLimiter()

        limiter.canDoFrame(0f)

        Assertions.assertFalse(limiter.canDoFrame(0.24f))
    }

    @Test
    fun canDoFrame_WhenFrameDeltaLarge_ReturnsTrue() {
        val limiter = createFrameLimiter()

        limiter.canDoFrame(0f)

        Assertions.assertTrue(limiter.canDoFrame(0.8f))
    }

    @Test
    fun canDoFrame_AfterManySmallIncrements_ReturnTrue() {
        val limiter = createFrameLimiter()

        limiter.canDoFrame(0.0f)
        limiter.canDoFrame(0.1f)
        limiter.canDoFrame(0.2f)

        Assertions.assertTrue(limiter.canDoFrame(0.3f))
    }

    @Test
    fun testGetFrameDelta_WhenOverlapping() {
        val limiter = createFrameLimiter()

        limiter.canDoFrame(0.8f)

        Assertions.assertEquals(0.4f, limiter.getFrameDelta(0.2f), 0.0001f)
    }

    @Test
    fun testGetFrameDelta() {
        val limiter = createFrameLimiter()

        limiter.canDoFrame(0.2f)

        Assertions.assertEquals(0.1f, limiter.getFrameDelta(0.3f), 0.0001f)
    }

    private fun createFrameLimiter(): FrameLimiter = FrameLimiter(4f)
}