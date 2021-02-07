package net.barribob.boss.cardinalComponents

import net.barribob.maelstrom.general.data.HistoricalData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Vec3d

class PlayerMoveHistory(val player: PlayerEntity) : IPlayerMoveHistoryComponent {
    private val positionalHistory = HistoricalData<Vec3d>(Vec3d.ZERO, 10)

    override fun serverTick() {
        val previousPosition = positionalHistory.get()
        val newPosition = player.pos

        // Extremely fast movement in one tick is a sign of teleportation or dimension hopping, and thus we should clear history to avoid undefined behavior
        if (previousPosition.squaredDistanceTo(newPosition) > 5) {
            positionalHistory.clear()
        }

        positionalHistory.set(newPosition)
    }

    override fun getHistoricalPositions(): List<Vec3d> = positionalHistory.getAll()

    override fun readFromNbt(p0: CompoundTag) {
    }

    override fun writeToNbt(p0: CompoundTag) {
    }
}