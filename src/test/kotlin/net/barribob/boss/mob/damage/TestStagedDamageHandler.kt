package net.barribob.boss.mob.damage

import net.barribob.boss.mob.utils.IEntityStats
import net.minecraft.entity.damage.DamageSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestStagedDamageHandler {
    @Test
    fun whenThresholdCrossed_CallsCallback() {
        var called = false
        val beforeStats = StubEntityStats(1f, 0.8f)
        val afterStats = StubEntityStats(1f, 0.7f)
        val damageHandler = StagedDamageHandler(listOf(0.0f, 0.75f, 1.0f)) { called = true }

        damageHandler.beforeDamage(beforeStats, DamageSource.ANVIL, 0f)
        damageHandler.afterDamage(afterStats, DamageSource.ANVIL, 0f, true)

        Assertions.assertTrue(called)
    }

    @Test
    fun whenThresholdNotCrossed_NoCall() {
        var called = false
        val beforeStats = StubEntityStats(1f, 0.8f)
        val afterStats = StubEntityStats(1f, 0.7f)
        val damageHandler = StagedDamageHandler(listOf(0.0f, 0.6f, 1.0f)) { called = true }

        damageHandler.beforeDamage(beforeStats, DamageSource.ANVIL, 0f)
        damageHandler.afterDamage(afterStats, DamageSource.ANVIL, 0f, true)

        Assertions.assertFalse(called)
    }

    class StubEntityStats(private val max_health: Float, private val health: Float) : IEntityStats {
        override fun getMaxHealth(): Float = max_health
        override fun getHealth(): Float = health
    }
}