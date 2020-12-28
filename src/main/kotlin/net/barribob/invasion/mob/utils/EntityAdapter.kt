package net.barribob.invasion.mob.utils

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.Vec3d

class EntityAdapter(val entity: LivingEntity): IEntity {
    override fun getVel(): Vec3d = entity.velocity
    override fun getPos(): Vec3d = entity.pos
    override fun getRotationVector(): Vec3d = entity.rotationVector
    override fun getAge(): Int = entity.age
    override fun isAlive(): Boolean = entity.isAlive
    override fun target(): IEntity? {
        if(entity is MobEntity) {
            val target = entity.target
            if(target != null) {
                return EntityAdapter(target)
            }
        }

        return null
    }
}