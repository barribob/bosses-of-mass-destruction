package net.barribob.maelstrom.general.io

interface ILogger {
    fun error(message: Any)
    fun warn(message: Any)
    fun info(message: Any)
}