package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.BaseEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class GauntletEntity(entityType: EntityType<out PathAwareEntity>, world: World) : BaseEntity(entityType, world) {
    override fun registerControllers(data: AnimationData) {
    }
}