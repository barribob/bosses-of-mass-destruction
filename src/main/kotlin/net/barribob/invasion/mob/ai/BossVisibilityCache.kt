package net.barribob.invasion.mob.ai

import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.MobVisibilityCache

class BossVisibilityCache(private val owner: MobEntity) : MobVisibilityCache(owner) {
    override fun canSee(entity: Entity?): Boolean = owner.target != null || super.canSee(entity)
}