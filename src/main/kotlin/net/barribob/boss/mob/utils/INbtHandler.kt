package net.barribob.boss.mob.utils

import net.minecraft.nbt.CompoundTag

interface INbtHandler {
    fun toTag(tag: CompoundTag): CompoundTag
    fun fromTag(tag: CompoundTag)
}