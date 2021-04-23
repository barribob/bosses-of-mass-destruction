package net.barribob.boss.particle

import net.barribob.boss.animation.IAnimationTimer
import net.barribob.boss.render.FrameLimiter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestFrameLimiter {
    @Test
    fun canDoFrame_WhenFrameDeltaTooSmall_ReturnFalse() {
        val time = 0.24
        val timer = StubAnimationTimer { time }
        val limiter = createFrameLimiter(timer)

        Assertions.assertFalse(limiter.canDoFrame())
    }

    @Test
    fun canDoFrame_WhenFrameDeltaLarge_ReturnsTrue() {
        val time = 0.26
        val timer = StubAnimationTimer { time }
        val limiter = createFrameLimiter(timer)

        Assertions.assertTrue(limiter.canDoFrame())
    }

    @Test
    fun canDoFrame_AfterManySmallIncrements() {
        var time = 0.2
        val timer = StubAnimationTimer { time }
        val limiter = createFrameLimiter(timer)

        val firstTime = limiter.canDoFrame()
        time = 0.3
        val secondTime = limiter.canDoFrame()
        time = 0.4
        val thirdTime = limiter.canDoFrame()

        Assertions.assertFalse(firstTime)
        Assertions.assertTrue(secondTime)
        Assertions.assertFalse(thirdTime)
    }

    private fun createFrameLimiter(timer: IAnimationTimer): FrameLimiter = FrameLimiter(4f, timer)

    private class StubAnimationTimer(val time: () -> Double): IAnimationTimer {
        override fun getCurrentTick(): Double = time()
    }
}