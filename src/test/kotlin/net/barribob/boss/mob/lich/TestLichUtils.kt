package net.barribob.boss.mob.lich

import net.barribob.boss.mob.damage.TestStagedDamageHandler
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.testing_utilities.StubEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

    @ParameterizedTest
    @CsvSource(
        "8f, 3f, 1f",
        "4f, 3f, 0f",
        "5f, 3f, 3f",
        "10f, 1f, 0f",
    )
    fun testCappedHeal(currentHealth: Float, heal: Float, result: Float) {
        val stubEntity = StubEntity()
        val stubEntityStats = TestStagedDamageHandler.StubEntityStats(10f, currentHealth)
        val hpPercents = listOf(0f, 0.25f, 0.5f, 1f)
        var healResult = 0f
        LichUtils.cappedHeal(stubEntity, stubEntityStats, hpPercents, heal) { healResult = it }
        Assertions.assertEquals(result, healResult, 0.001f)
    }
}