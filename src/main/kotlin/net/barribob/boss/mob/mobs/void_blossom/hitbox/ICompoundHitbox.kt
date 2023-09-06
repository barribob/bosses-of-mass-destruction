package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds

interface ICompoundHitbox {
    fun updatePosition()
    fun getBounds(): EntityBounds
    fun setNextDamagedPart(part: String?)
}