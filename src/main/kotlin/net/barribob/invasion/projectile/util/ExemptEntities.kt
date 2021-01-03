package net.barribob.invasion.projectile.util

import net.minecraft.entity.EntityType
import net.minecraft.util.hit.EntityHitResult
import java.util.function.Predicate

class ExemptEntities(private val exemptEntities: List<EntityType<*>>): Predicate<EntityHitResult> {
    override fun test(t: EntityHitResult): Boolean =
        !exemptEntities.contains(t.entity.type)
}