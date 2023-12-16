package net.barribob.maelstrom.general.event

interface IEvent {
    fun shouldDoEvent(): Boolean
    fun doEvent()
    fun shouldRemoveEvent(): Boolean
    fun tickSize(): Int
}