package net.barribob.boss.render

import net.barribob.boss.animation.IAnimationTimer

class FrameLimiter(framesPerUnit: Float, private val timer: IAnimationTimer) {
    private val minimumFrameDelta = 1 / framesPerUnit
    var previousTime = 0.0

    fun canDoFrame(): Boolean {
        val currentTick = timer.getCurrentTick()
        val frameDelta = currentTick - previousTime
        if (frameDelta >= minimumFrameDelta) {
            previousTime = currentTick
            return true
        }
        return false
    }
}