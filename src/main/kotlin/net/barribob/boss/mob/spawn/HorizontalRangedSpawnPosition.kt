package net.barribob.boss.mob.spawn

import net.barribob.maelstrom.general.random.IRandom
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.util.math.Vec3d

class HorizontalRangedSpawnPosition(
    private val position: Vec3d,
    private val minDistance: Double,
    private val maxDistance: Double,
    private val random: IRandom,
) : ISpawnPosition {
    override fun getPos(): Vec3d {
        val randomOffset = random.getVector().normalize()
        val horizontalAddition = randomOffset.planeProject(VecUtils.yAxis).normalize()
        val coercedRandomOffset = randomOffset.add(horizontalAddition).normalize()
            .multiply(randomOffset.length()
                .coerceAtLeast(minDistance)
                .coerceAtMost(maxDistance))
        return position.add(coercedRandomOffset)
    }
}