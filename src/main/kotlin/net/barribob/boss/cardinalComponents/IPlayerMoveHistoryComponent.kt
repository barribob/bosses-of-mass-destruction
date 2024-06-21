package net.barribob.boss.cardinalComponents

import net.minecraft.util.math.Vec3d
import org.ladysnake.cca.api.v3.component.ComponentV3
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent

interface IPlayerMoveHistoryComponent: ComponentV3, ServerTickingComponent {
    fun getHistoricalPositions(): List<Vec3d>
}