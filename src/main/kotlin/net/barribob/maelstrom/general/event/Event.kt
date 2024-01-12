package net.barribob.maelstrom.general.event

class Event(
        val condition: () -> Boolean,
        val callback: () -> Unit,
        val shouldCancel: () -> Boolean = { false },
        val tickSize: Int = 1) : IEvent {

    override fun shouldDoEvent(): Boolean = condition()

    override fun doEvent() = callback()

    override fun shouldRemoveEvent(): Boolean = shouldCancel()

    override fun tickSize(): Int = tickSize
}