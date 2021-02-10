package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.util.math.Vec3d

object ObsidilithUtils {
    const val burstAttackStatus: Byte = 5
    const val waveAttackStatus: Byte = 6
    const val spikeAttackStatus: Byte = 7
    const val anvilAttackStatus: Byte = 8

    fun approximatePlayerNextPosition(previousPosition: List<Vec3d>, currentPos: Vec3d): Vec3d {
        return previousPosition
            .map { it.subtract(currentPos).planeProject(VecUtils.yAxis) }
            .reduce { acc, vec3d -> acc.add(vec3d) }
            .multiply(-0.5).add(currentPos)
    }
}