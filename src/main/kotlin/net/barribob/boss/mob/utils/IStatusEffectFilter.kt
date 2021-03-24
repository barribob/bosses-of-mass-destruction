package net.barribob.boss.mob.utils

import net.minecraft.entity.effect.StatusEffectInstance

interface IStatusEffectFilter {
    fun canHaveStatusEffect(effect: StatusEffectInstance): Boolean
}