package net.barribob.invasion.mob.ai.action

import net.barribob.invasion.mob.utils.ProjectileThrower
import net.minecraft.entity.mob.PathAwareEntity

class ThrowProjectileAction(val entity: PathAwareEntity, private val projectileThrower: ProjectileThrower) : IAction {
    override fun perform() {
        entity.target?.let { projectileThrower.throwProjectile(it.boundingBox.center) }
    }
}