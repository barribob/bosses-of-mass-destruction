package net.barribob.maelstrom.general.random

import java.util.*

// https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
class WeightedRandom<E> (private val random: Random = Random()) {
    private val map: NavigableMap<Double, E> = TreeMap()
    private var total = 0.0

    fun add(weight: Double, result: E): WeightedRandom<E> {
        if (weight <= 0) return this
        require(!(weight.isNaN() || weight.isInfinite())) { "The weight for random collection is invalid: $weight" }
        total += weight
        map[total] = result
        return this
    }

    fun addAll(collection: Collection<Pair<Double, E>>){
        for (pair in collection) {
            add(pair.first, pair.second)
        }
    }

    operator fun next(): E {
        val value = random.nextDouble() * total
        return map.higherEntry(value).value
    }
}