package net.barribob.invasion.mob.ai.action

import net.barribob.invasion.utils.VanillaCopies
import net.minecraft.entity.mob.PathAwareEntity

class SnowballThrowAction(val entity: PathAwareEntity) : IAction {
    override fun perform() {
        entity.target?.let { VanillaCopies.attack(entity, it) }
    }
}