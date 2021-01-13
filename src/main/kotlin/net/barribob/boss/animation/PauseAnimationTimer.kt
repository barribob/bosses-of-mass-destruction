package net.barribob.boss.animation

class PauseAnimationTimer(val sysTimeProvider: () -> Double, val isPaused: () -> Boolean) : IAnimationTimer {
    private var pauseTime = 0.0
    private var pauseStart = 0.0

    override fun getCurrentTick(): Float {
        val sysTime = sysTimeProvider()

        if (isPaused()) {
            if (pauseStart == 0.0) {
                pauseStart = sysTime
            }
            return (pauseStart - pauseTime).toFloat()

        } else if (pauseStart != 0.0) {
            val timeElapsed = sysTime - pauseStart
            pauseTime += timeElapsed
            pauseStart = 0.0
        }

        return (sysTime - pauseTime).toFloat()
    }
}