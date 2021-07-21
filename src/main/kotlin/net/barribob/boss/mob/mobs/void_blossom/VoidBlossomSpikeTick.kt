package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import kotlin.math.pow

class VoidBlossomSpikeTick(private val entity: VoidBlossomEntity) : IEntityTick<ServerWorld>{
    override fun tick(world: ServerWorld) {
        val spikeHitbox = Box(entity.pos, entity.pos).expand(3.0, 3.0, 3.0).offset(0.0, 1.5, 0.0)
        val targets = world.getEntitiesByClass(LivingEntity::class.java, spikeHitbox) { it != entity }
        for(target in targets) {
            val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            if(target.pos.squaredDistanceTo(entity.pos) < 3.0.pow(2)) {
                target.damage(DamageSource.thorns(entity), damage)
            }
        }
    }
}