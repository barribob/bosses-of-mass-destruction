package net.barribob.boss.block

import net.minecraft.util.StringIdentifiable

enum class TripleBlockPart : StringIdentifiable {
    TOP,
    MIDDLE,
    BOTTOM;

    override fun toString(): String {
        return asString()
    }

    override fun asString(): String {
        return when {
            this == TOP -> "top"
            this == MIDDLE -> "middle"
            else -> "bottom"
        }
    }
}