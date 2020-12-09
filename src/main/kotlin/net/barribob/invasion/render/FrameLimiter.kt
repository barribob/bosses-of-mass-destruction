package net.barribob.invasion.render

import net.barribob.invasion.animation.IAnimationTimer

class FrameLimiter(framesPerUnit: Float, private val timer: IAnimationTimer) {
    private val minimumFrameDelta = 1 / framesPerUnit
    var previousTime = 0f

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