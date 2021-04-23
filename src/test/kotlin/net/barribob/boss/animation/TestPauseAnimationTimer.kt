package net.barribob.boss.animation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestPauseAnimationTimer {
    @Test
    fun getCurrentTick_WhenNoPause_ReturnsSystemTime() {
        val timeProvider = { 10.0 }
        val timer = PauseAnimationTimer(timeProvider, { false })

        val actualTime = timer.getCurrentTick()

        Assertions.assertEquals(10.0, actualTime)
    }

    @Test
    fun getCurrentTick_WhenPaused_ReturnsFirstPausedTime() {
        var time = 10.0
        val isPaused = true
        val timer = PauseAnimationTimer({ time }, { isPaused })

        timer.getCurrentTick()
        time = 11.0

        Assertions.assertEquals(10.0, timer.getCurrentTick())
    }

    @Test
    fun getCurrentTick_WhenUnpaused_ReturnsSystemTimeMinusPausedTime() {
        var time = 10.0
        val timeWhenUnpaused = 12.0
        var isPaused = true
        val timer = PauseAnimationTimer({ time }, { isPaused })

        timer.getCurrentTick()
        time = timeWhenUnpaused
        isPaused = false
        timer.getCurrentTick()
        time = 13.0

        Assertions.assertEquals(11.0, timer.getCurrentTick())
    }
}