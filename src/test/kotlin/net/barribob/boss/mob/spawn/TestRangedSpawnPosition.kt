package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.random.IRandom
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestRangedSpawnPosition {
    @Test
    fun positionChosenCloserThanMaxRange() {
        val spawnPosition = RangedSpawnPosition(Vec3d.ZERO, 1.0, 2.0, StubRandom({ VecUtils.yAxis.multiply(3.0) }, { .0 }))

        Assertions.assertEquals(2.0, spawnPosition.getPos().distanceTo(Vec3d.ZERO))
    }

    @Test
    fun positionChosenFartherThanMinRange() {
        val spawnPosition = RangedSpawnPosition(Vec3d.ZERO, 1.0, 2.0, StubRandom({ VecUtils.yAxis.multiply(0.5) }, { .0 }))

        Assertions.assertEquals(1.0, spawnPosition.getPos().distanceTo(Vec3d.ZERO))
    }

    @Test
    fun positionChosenWithinRangeDoesNotGetAffected() {
        val spawnPosition = RangedSpawnPosition(Vec3d.ZERO, 1.0, 2.0, StubRandom({ VecUtils.yAxis.multiply(1.5) }, { .0 }))

        Assertions.assertEquals(1.5, spawnPosition.getPos().distanceTo(Vec3d.ZERO))
    }

    private class StubRandom(
        val randomVec: () -> Vec3d,
        val randomDouble: () -> Double
    ) : IRandom {
        override fun getDouble(): Double = randomDouble()
        override fun getVector(): Vec3d = randomVec()
    }
}