package net.barribob.boss.render

import net.minecraft.util.Identifier

fun interface ITextureProvider <T> {
    fun getTexture(entity: T): Identifier
}