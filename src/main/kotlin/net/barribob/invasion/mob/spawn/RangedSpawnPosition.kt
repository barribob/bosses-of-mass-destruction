package net.barribob.invasion.mob.spawn

import net.barribob.maelstrom.general.random.IRandom
import net.minecraft.util.math.Vec3d

class RangedSpawnPosition(
    private val position: () -> Vec3d,
    private val minDistance: Double,
    private val maxDistance: Double,
    private val random: IRandom,
) : ISpawnPosition {
    override fun getPos(): Vec3d {
        val centralPosition = position()
        val randomOffset = random.getVector()
        val coercedRandomOffset = randomOffset.normalize()
            .multiply(randomOffset.length()
                .coerceAtLeast(minDistance)
                .coerceAtMost(maxDistance))
        return centralPosition.add(coercedRandomOffset)
    }
}