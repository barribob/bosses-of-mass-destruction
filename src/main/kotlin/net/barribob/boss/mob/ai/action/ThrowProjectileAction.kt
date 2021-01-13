package net.barribob.boss.mob.ai.action

import net.barribob.boss.mob.utils.ProjectileThrower
import net.minecraft.entity.mob.PathAwareEntity

class ThrowProjectileAction(val entity: PathAwareEntity, private val projectileThrower: ProjectileThrower) : IAction {
    override fun perform() {
        entity.target?.let { projectileThrower.throwProjectile(it.boundingBox.center) }
    }
}