package net.barribob.boss.cardinalComponents

import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.world.World

interface IWorldEventScheduler {
    fun getWorldEventScheduler(world: World): EventScheduler
}