package net.barribob.invasion.render

class FrameLimiter(framesPerTick: Float) {
    private val minimumFrameDelta = 1 / framesPerTick
    var previousPartialTicks = -1.0f

    fun canDoFrame(partialTicks: Float): Boolean {
        if (getFrameDelta(partialTicks) >= minimumFrameDelta) {
            previousPartialTicks = partialTicks
            return true
        }
        return false
    }

    fun getFrameDelta(partialTicks: Float): Float {
        return if (partialTicks > previousPartialTicks) {
            partialTicks - previousPartialTicks
        } else {
            1 - previousPartialTicks + partialTicks
        }
    }
}