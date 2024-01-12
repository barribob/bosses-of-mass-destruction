package net.barribob.maelstrom.general.data

import java.util.*
import java.util.function.Predicate

class WeakHashPredicate<T> (val predicateFactory: () -> () -> Boolean): Predicate<T> {
    private val conditionals = WeakHashMap<T, () -> Boolean>()

    override fun test(t: T): Boolean {
        if(!conditionals.containsKey(t)) {
            conditionals[t] = predicateFactory()
        }

        return conditionals[t]?.invoke() == true
    }
}