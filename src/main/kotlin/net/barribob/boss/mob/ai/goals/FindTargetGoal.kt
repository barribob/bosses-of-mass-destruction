package net.barribob.boss.mob.ai.goals

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.Box
import java.util.function.Predicate

class FindTargetGoal<T : LivingEntity>(
    mob: MobEntity,
    targetClass: Class<T>,
    private val searchBoxProvider: (Double) -> Box,
    reciprocalChance: Int = 10,
    checkVisibility: Boolean = true,
    checkCanNavigate: Boolean = false,
    targetPredicate: Predicate<LivingEntity>? = null
) : FollowTargetGoal<T>(
    mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate
) {
    override fun getSearchBox(distance: Double): Box = searchBoxProvider(distance)
}