package net.barribob.maelstrom.general

import kotlin.random.Random

object RandomUtils {
    private val rand = Random

    fun double(range: Double): Double {
        return (rand.nextDouble() - 0.5) * 2 * range
    }
}
