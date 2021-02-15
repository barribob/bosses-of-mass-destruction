package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities.OBSIDILITH
import net.barribob.boss.render.NodeBossBarRenderer
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.util.math.Vec3d

object ObsidilithUtils {
    private const val textureSize = 256
    private val bossBarDividerTexture = Mod.identifier("textures/gui/obsidilith_boss_bar_dividers.png")
    const val burstAttackStatus: Byte = 5
    const val waveAttackStatus: Byte = 6
    const val spikeAttackStatus: Byte = 7
    const val anvilAttackStatus: Byte = 8
    const val pillarDefenseStatus: Byte = 9
    val hpPillarShieldMilestones = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    val isShielded: TrackedData<Boolean> =
        DataTracker.registerData(ObsidilithEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
    val obsidilithBossBarRenderer =
        NodeBossBarRenderer(OBSIDILITH.translationKey, hpPillarShieldMilestones, bossBarDividerTexture, textureSize)

    fun approximatePlayerNextPosition(previousPosition: List<Vec3d>, currentPos: Vec3d): Vec3d {
        return previousPosition
            .map { it.subtract(currentPos).planeProject(VecUtils.yAxis) }
            .reduce { acc, vec3d -> acc.add(vec3d) }
            .multiply(-0.5).add(currentPos)
    }
}