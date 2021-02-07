package net.barribob.boss.cardinalComponents

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

interface IPlayerMoveHistory {
    fun getPlayerPositions(serverPlayerEntity: ServerPlayerEntity): List<Vec3d>
}