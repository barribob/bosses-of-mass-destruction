package net.barribob.boss.mob.utils

import net.minecraft.nbt.NbtCompound

interface INbtHandler {
    fun writeNbt(tag: NbtCompound): NbtCompound
    fun fromNbt(tag: NbtCompound)
}