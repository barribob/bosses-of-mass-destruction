package net.barribob.invasion.mob.ai.action

fun interface IActionWithCooldown {
    fun perform(): Int
}