package net.barribob.maelstrom.general.event

/**
 * Manages cancelable scheduled events to run in the future.
 *
 * Note that there is no persistence with this system (yet!). This means that long
 * term events are not ideal.
 */
class EventScheduler {
    private var ticks = 0
    private val eventQueue = mutableListOf<IEvent>()
    private val eventsToAdd = mutableSetOf<IEvent>()

    fun updateEvents() {
        eventQueue.addAll(eventsToAdd)
        eventsToAdd.clear()
        for (iEvent in eventQueue) {
            if(ticks % iEvent.tickSize() == 0 && iEvent.shouldDoEvent()) {
                iEvent.doEvent()
            }
        }
        eventQueue.removeAll { it.shouldRemoveEvent() }

        this.ticks++
    }

    fun addEvent(event: IEvent) {
        eventsToAdd.add(event)
    }
}