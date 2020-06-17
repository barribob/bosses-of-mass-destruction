package net.barribob.maelstrom.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity

class AIManager {
    val injections = mutableMapOf<EntityType<out MobEntity>, MutableList<(MobEntity) -> Pair<Int, Goal>>>()

    fun addGoalInjection(entityId: EntityType<out MobEntity>, generator: (MobEntity) -> Pair<Int, Goal>) {
        if(!injections.containsKey(entityId)) {
            injections[entityId] = mutableListOf()
        }

        injections[entityId]?.add(generator)
    }
}