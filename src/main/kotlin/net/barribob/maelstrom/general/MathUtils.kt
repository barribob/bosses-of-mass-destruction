package net.barribob.maelstrom.general

import kotlin.math.pow
import kotlin.math.sqrt

object MathUtils {
    /**
     * Treats input as a vector and finds the length of that vector
     */
    fun magnitude(vararg values: Double): Double {
        var sum = 0.0
        for (value in values) {
            sum += value.pow(2.0)
        }
        return sqrt(sum)
    }
}