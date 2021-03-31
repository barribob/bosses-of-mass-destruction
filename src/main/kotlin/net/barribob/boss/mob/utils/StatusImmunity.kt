package net.barribob.boss.mob.utils

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance

class StatusImmunity(vararg statusEffects: StatusEffect): IStatusEffectFilter {
    private val statusEffectList = statusEffects.toList()

    override fun canHaveStatusEffect(effect: StatusEffectInstance): Boolean {
        return !statusEffectList.contains(effect.effectType)
    }
}