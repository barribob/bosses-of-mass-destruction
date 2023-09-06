package net.barribob.boss.mob.utils

import net.minecraft.nbt.NbtCompound

interface INbtHandler {
    fun toTag(tag: NbtCompound): NbtCompound
    fun fromTag(tag: NbtCompound)
}