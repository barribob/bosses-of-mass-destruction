package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.utils.BaseEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class VoidBlossomEntity(entityType: EntityType<out PathAwareEntity>, world: World) : BaseEntity(entityType, world) {
    override val statusHandler = AnimationHolder(this, mapOf())
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PINK, BossBar.Style.NOTCHED_12)

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        statusHandler.registerControllers(data)
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }
}