package net.barribob.maelstrom.general

import java.util.*

/**
 * Class that handles scheduled events to run in the future.
 * Events are linked to an object's existence, meaning that if the object is
 * garbage collected, the event will not run.
 *
 * Note that there is no persistence with this system (yet!). This means that long
 * term events are not ideal.
 */
class EventScheduler {
    private val eventsMap = WeakHashMap<Any, PriorityQueue<TimedEvent>>()
    private val queuedEvents = WeakHashMap<Any, MutableList<TimedEvent>>()
    var ticks = 0

    fun updateEvents() {
        for (pair in queuedEvents) {
            if (!eventsMap.containsKey(pair.key)) {
                eventsMap[pair.key] = PriorityQueue()
            }

            for (event in pair.value) {
                eventsMap[pair.key]?.add(TimedEvent(event.callback, event.ticks + this.ticks))
            }
        }

        queuedEvents.clear()

        for (pair in eventsMap) {
            var itr = pair.value.iterator()

            while (itr.hasNext()) {
                val event = itr.next()

                if (event.ticks <= this.ticks) {
                    itr.remove()
                    event.callback()
                } else {
                    break
                }
            }
        }

        this.ticks++
    }

    /**
     * Adds a scheduled event, attached to a certain object's existence.
     */
    fun addEvent(obj: Any, associatedObject: () -> Unit, ticksFromNow: Int) {
        if (!queuedEvents.containsKey(obj)) {
            queuedEvents[obj] = mutableListOf()
        }

        queuedEvents[obj]?.add(TimedEvent(associatedObject, ticksFromNow))
    }

    private class TimedEvent(val callback: () -> Unit, val ticks: Int) : Comparable<TimedEvent> {
        override fun compareTo(other: TimedEvent): Int = this.ticks - other.ticks
    }
}

