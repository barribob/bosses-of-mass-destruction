package net.barribob.maelstrom.general.data

class BooleanFlag {
    private var flag: Boolean = false

    fun flag() {
        flag = true
    }

    fun getAndReset(): Boolean {
        val toReturn = flag
        if (flag) flag = false
        return toReturn
    }
}