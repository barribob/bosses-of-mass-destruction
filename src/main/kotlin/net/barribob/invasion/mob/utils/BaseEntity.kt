package net.barribob.invasion.mob.utils

import net.barribob.invasion.Invasions
import net.barribob.invasion.mob.utils.animation.GeckolibAnimationManager
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.static_utilities.NbtUtils
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
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


    init {
        val mobsConfig = MaelstromMod.configRegistry.getConfig(Identifier(Invasions.MODID, "mobs"))
        val id = Registry.ENTITY_TYPE.getId(entityType)
        if (id != Registry.ENTITY_TYPE.defaultId && mobsConfig.hasPath(id.path)) {
            val entityConfig = mobsConfig.getConfig(id.path)
            val nbtKey = "default_nbt"
            if (entityConfig.hasPath(nbtKey)) {
                val nbt = NbtUtils.readDefaultNbt(Invasions.LOGGER, entityConfig.getConfig(nbtKey))
                fromTag(nbt)
            }
        }
    }

    final override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
    }

    final override fun readCustomDataFromTag(tag: CompoundTag?) {
        super.readCustomDataFromTag(tag)
    }
}