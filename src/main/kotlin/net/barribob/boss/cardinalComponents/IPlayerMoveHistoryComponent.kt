package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentV3
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d

interface IPlayerMoveHistoryComponent: ComponentV3, ServerTickingComponent {
    fun getHistoricalPositions(): List<Vec3d>

    override fun readFromNbt(p0: NbtCompound) {
    }

    override fun writeToNbt(p0: NbtCompound) {
    }
}