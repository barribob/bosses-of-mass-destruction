package net.barribob.invasion.mob.utils

import net.barribob.invasion.mob.utils.animation.GeckolibAnimationManager
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.world.World
import software.bernie.geckolib.entity.IAnimatedEntity
import software.bernie.geckolib.manager.EntityAnimationManager

abstract class BaseEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world), IAnimatedEntity {
    protected abstract fun initializeGeckoManager(): GeckolibAnimationManager<out BaseEntity>
    override fun getAnimationManager(): EntityAnimationManager = geckoManager.animationManager
    val geckoManager: GeckolibAnimationManager<out BaseEntity> by lazy {
        val manager = initializeGeckoManager()
        manager
    }
}