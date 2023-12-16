package net.barribob.maelstrom.general.event

class EventSeries(vararg events: IEvent) : IEvent {
    val iterator = events.iterator()
    var currentEvent = if(iterator.hasNext()) iterator.next() else throw IllegalArgumentException("Must have at least one event")

    override fun shouldDoEvent(): Boolean = currentEvent.shouldDoEvent()

    override fun doEvent() {
        currentEvent.doEvent()
    }

    override fun shouldRemoveEvent(): Boolean {
        while (currentEvent.shouldRemoveEvent()) {
            if (iterator.hasNext()) {
                currentEvent = iterator.next()
            } else {
                return true
            }
        }
        return false
    }

    override fun tickSize(): Int = currentEvent.tickSize()
}