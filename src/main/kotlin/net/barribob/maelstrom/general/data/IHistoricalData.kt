package net.barribob.maelstrom.general.data

interface IHistoricalData<T> {
    fun set(value: T)
    fun get(past: Int = 0): T
    fun getAll(): Collection<T>
}