package net.barribob.boss.cardinalComponents

import net.minecraft.entity.player.PlayerEntity

interface ILichSummonCounter {
    fun getLichSummons(playerEntity: PlayerEntity): Int
    fun increment(playerEntity: PlayerEntity)
}