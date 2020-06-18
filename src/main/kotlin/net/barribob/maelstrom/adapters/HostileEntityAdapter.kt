package net.barribob.maelstrom.adapters

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.world.World

/**
 * Abstract base class for mobs. Not ideal in its architecture, but the goal to allow easier updates by having most of the minecraft specific
 * stuff in there so I only have to heavily update this class instead of every entity class
 */
open abstract class HostileEntityAdapter(entityType: EntityType<out HostileEntity>, world: World) : HostileEntity(entityType, world), IHostileEntity {

    override fun getAttributes(): AttributeContainer {
        val attributes = mutableListOf<Pair<EntityAttribute, Double>>()
        this.getAttributes(attributes)
        val attributeBuilder = createHostileAttributes()
        attributes.forEach{ attributeBuilder.add(it.first, it.second) }
        return AttributeContainer(attributeBuilder.build())
    }

    override fun initGoals() {
        val goals = mutableListOf<Pair<Int, IGoal>>()
        val targetGoals = mutableListOf<Pair<Int, IGoal>>()
        this.getGoals(goals, targetGoals)
        goals.forEach { goalSelector.add(it.first, GoalAdapter(it.second)) }
        targetGoals.forEach { targetSelector.add(it.first, GoalAdapter(it.second)) }
        super.initGoals()
    }
}