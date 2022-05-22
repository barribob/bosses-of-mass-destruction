package net.barribob.boss.mob.lich

import net.barribob.boss.mob.damage.TestStagedDamageHandler
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.testing_utilities.StubEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TestLichUtils {
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