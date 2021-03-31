package net.barribob.boss.mob.utils

class CompositeStatusHandler(vararg statusHandlers: IStatusHandler): IStatusHandler {
    private val statusHandlerList = statusHandlers.asList()

    override fun handleClientStatus(status: Byte) {
        statusHandlerList.forEach { it.handleClientStatus(status) }
    }
}