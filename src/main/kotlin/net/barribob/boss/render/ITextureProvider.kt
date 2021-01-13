package net.barribob.boss.render

import net.minecraft.entity.Entity
import net.minecraft.util.Identifier

fun interface ITextureProvider <T : Entity> {
    fun getTexture(entity: T): Identifier
}