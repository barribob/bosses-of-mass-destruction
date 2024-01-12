package net.barribob.maelstrom.general.data

// Todo: Implement list/collection interface
class HistoricalData<T>(defaultValue: T, val maxHistory: Int = 2) : IHistoricalData<T> {
    private val history = mutableListOf(defaultValue)

    init {
        if (maxHistory < 2) throw IllegalArgumentException("Max History cannot be less than 2")
    }

    override fun set(value: T) {
        history.add(value)
        if (history.size > maxHistory) {
            history.removeAt(0)
        }
    }

    override fun get(past: Int): T {
        if (past < 0) throw IllegalArgumentException("Past cannot be negative")

        val clampedPast = (history.size - 1 - past).coerceAtLeast(0)
        return history[clampedPast]
    }

    override fun getAll() = history.toList()
    fun getSize() = history.size

    fun clear() {
        history.clear()
    }
}