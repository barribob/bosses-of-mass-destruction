package net.barribob.invasion.mob.utils

import net.barribob.invasion.Invasions
import net.barribob.invasion.utils.IVelPos
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.static_utilities.NbtUtils
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.manager.AnimationFactory

abstract class BaseEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world), IAnimatable, IVelPos {
    private val animationFactory: AnimationFactory by lazy { AnimationFactory(this) }
    override fun getFactory(): AnimationFactory = animationFactory
    var idlePosition: Vec3d = Vec3d.ZERO // TODO: I don't actually know if this implementation works

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

    final override fun tick() {
        if (idlePosition == Vec3d.ZERO) idlePosition = pos
        super.tick()
    }

    final override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
    }

    final override fun readCustomDataFromTag(tag: CompoundTag?) {
        super.readCustomDataFromTag(tag)
    }

    final override fun setTarget(target: LivingEntity?) {
        if(target == null) idlePosition = pos
        super.setTarget(target)
    }

    override fun getVel(): Vec3d = velocity
}