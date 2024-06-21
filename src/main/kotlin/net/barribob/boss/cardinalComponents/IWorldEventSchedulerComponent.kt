package net.barribob.boss.cardinalComponents

import net.barribob.maelstrom.general.event.EventScheduler
import org.ladysnake.cca.api.v3.component.ComponentV3
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent

interface IWorldEventSchedulerComponent : ComponentV3, ServerTickingComponent, ClientTickingComponent {
    fun get(): EventScheduler
}