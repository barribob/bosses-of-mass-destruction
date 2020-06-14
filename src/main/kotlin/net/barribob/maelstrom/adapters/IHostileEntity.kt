package net.barribob.maelstrom.adapters

import net.minecraft.entity.attribute.EntityAttribute

interface IHostileEntity {
    fun getAttributes(attributes: MutableList<Pair<EntityAttribute, Double>>)

    fun getGoals(goals: MutableList<Pair<Int, IGoal>>, targetGoals: MutableList<Pair<Int, IGoal>>)
}
