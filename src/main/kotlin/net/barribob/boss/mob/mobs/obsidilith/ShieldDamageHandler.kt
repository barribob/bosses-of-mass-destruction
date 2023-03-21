package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.utils.ModUtils.randomPitch
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.registry.tag.DamageTypeTags

class ShieldDamageHandler(val isShielded: () -> Boolean) : IDamageHandler {
    /**
     * Shield mechanic of [LivingEntity.damage]
     */
    override fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean {
        if(isShielded() && !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!damageSource.isIn(DamageTypeTags.IS_PROJECTILE)) {
                val entity: Entity? = damageSource.source
                if (entity is LivingEntity) {
                    entity.takeKnockback(0.5, actor.x - entity.getX(), actor.z - entity.getZ())
                }
            }
            actor.playSound(Mod.sounds.energyShield, 1.0f, actor.random.randomPitch())
            return false
        }
        return true
    }
}