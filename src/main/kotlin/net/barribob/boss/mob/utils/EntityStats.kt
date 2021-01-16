package net.barribob.boss.mob.utils

import net.minecraft.entity.LivingEntity

class EntityStats(val entity: LivingEntity): IEntityStats {
    override fun getMaxHealth(): Float = entity.maxHealth
    override fun getHealth(): Float = entity.health
}