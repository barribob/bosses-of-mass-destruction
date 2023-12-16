package net.barribob.maelstrom.general.io

import org.apache.logging.log4j.Logger

class ConsoleLogger(private val logger: Logger) : ILogger {
    override fun error(message: Any) = logger.error(message)
    override fun warn(message: Any) = logger.warn(message)
    override fun info(message: Any) = logger.info(message)
}