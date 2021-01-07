package net.barribob.invasion.mob.lich

import net.barribob.invasion.mob.mobs.lich.LichUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestLichUtils {
    private val fullDay = 24000L
    private val midnight = 16000L

    @Test
    fun timeToNighttime_SwitchesDayToNight() {
        val time = (fullDay * 2) + 5000
        val actual = LichUtils.timeToNighttime(time)
        val expected = (fullDay * 2) + midnight

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun timeToNighttime_StaysAtSameTime() {
        val time = (fullDay * 2) + midnight
        val actual = LichUtils.timeToNighttime(time)

        Assertions.assertEquals(time, actual)
    }

    @Test
    fun timeToNighttime_StaysAtSameTime_With20TickLag() {
        val time = (fullDay * 2) + midnight + 20
        val actual = LichUtils.timeToNighttime(time)
        val expected = (fullDay * 2) + midnight

        Assertions.assertEquals(expected, actual)
    }
}