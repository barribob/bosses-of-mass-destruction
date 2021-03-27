package net.barribob.boss.mob.utils

import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class MovementGuard(val entity: Entity) : IEntityTick<ServerWorld> {
    private var lastPos: Vec3d? = null

    override fun tick(world: ServerWorld) {
        val previousPos = lastPos

        if(previousPos != null && previousPos.distanceTo(entity.pos) > 8) {
            println("Hmm - fish")
        }

        lastPos = entity.pos
    }

}