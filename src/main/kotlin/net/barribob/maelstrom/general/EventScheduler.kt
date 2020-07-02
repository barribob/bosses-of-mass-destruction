package net.barribob.maelstrom.general

import java.util.*


/**
 * Manages cancelable scheduled events to run in the future.
 *
 * Note that there is no persistence with this system (yet!). This means that long
 * term events are not ideal.
 */
class EventScheduler {
    private var ticks = 0
    private val eventQueue = PriorityQueue<TimedEvent>()
    private val eventsToAdd = mutableSetOf<TimedEvent>()

    fun updateEvents() {
        for (event in eventsToAdd) {
            if(!event.shouldCancel()) {
                eventQueue.add(TimedEvent(event.shouldCancel, event.callback, event.ticks + this.ticks))
            }
        }

        eventsToAdd.clear()

        val itr = eventQueue.iterator()

        while (itr.hasNext()) {
            val event = itr.next()

            if(event.shouldCancel()) {
                itr.remove()
            } else if (event.ticks <= this.ticks) {
                itr.remove()
                event.callback()
            } else {
                break
            }
        }

        this.ticks++
    }

    /**
     * Adds a scheduled event that may be canceled up until the time it runs
     */
    fun addEvent(shouldCancel: () -> Boolean, callback: () -> Unit, ticksFromNow: Int) {
        eventsToAdd.add(TimedEvent(shouldCancel, callback, ticksFromNow))
    }

    private class TimedEvent(val shouldCancel: () -> Boolean, val callback: () -> Unit, val ticks: Int) : Comparable<TimedEvent> {
        override fun compareTo(other: TimedEvent): Int = this.ticks - other.ticks
    }
}

