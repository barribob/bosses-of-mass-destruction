package net.barribob.boss.mob.ai.action

fun interface IActionWithCooldown {
    fun perform(): Int
}