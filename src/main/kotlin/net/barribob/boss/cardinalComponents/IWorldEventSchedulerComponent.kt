package net.barribob.boss.cardinalComponents

import dev.onyxstudios.cca.api.v3.component.ComponentV3
import net.barribob.maelstrom.general.event.EventScheduler

interface IWorldEventSchedulerComponent : ComponentV3 {
    fun get(): EventScheduler
}