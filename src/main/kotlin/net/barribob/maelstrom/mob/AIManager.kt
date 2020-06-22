package net.barribob.maelstrom.mob

import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity

class AIManager {
    val targetInjections = mutableMapOf<EntityType<out MobEntity>, MutableList<(MobEntity) -> Pair<Int, Goal>>>()
    val injections = mutableMapOf<EntityType<out MobEntity>, MutableList<(MobEntity) -> Pair<Int, Goal>>>()

    fun addGoalInjection(entityId: EntityType<out MobEntity>, generator: (MobEntity) -> Pair<Int, Goal>) {
        if(!injections.containsKey(entityId)) {
            injections[entityId] = mutableListOf()
        }

        injections[entityId]?.add(generator)
    }

    fun addTargetInjection(entityId: EntityType<out MobEntity>, generator: (MobEntity) -> Pair<Int, Goal>) {
        if(!targetInjections.containsKey(entityId)) {
            targetInjections[entityId] = mutableListOf()
        }

        targetInjections[entityId]?.add(generator)
    }
}