package net.barribob.maelstrom.general.event

class TimedEvent(
        val callback: () -> Unit,
        val delay: Int,
        val duration: Int = 1,
        val shouldCancel: () -> Boolean = { false }) : IEvent {

    var age = 0

    override fun shouldDoEvent(): Boolean = age++ >= delay && !shouldCancel()

    override fun doEvent() = callback()

    override fun shouldRemoveEvent(): Boolean = shouldCancel() || age >= delay + duration

    override fun tickSize(): Int = 1
}