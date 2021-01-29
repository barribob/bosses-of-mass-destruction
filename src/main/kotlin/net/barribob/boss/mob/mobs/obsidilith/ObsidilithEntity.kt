package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.utils.BaseEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class ObsidilithEntity(entityType: EntityType<out ObsidilithEntity>, world: World) : BaseEntity(entityType, world){
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PINK, BossBar.Style.NOTCHED_12)

    override fun registerControllers(p0: AnimationData?) {
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun canHaveStatusEffect(effect: StatusEffectInstance): Boolean {
        return if (effect.effectType === StatusEffects.WITHER || effect.effectType === StatusEffects.POISON) {
            false
        }
        else{
            super.canHaveStatusEffect(effect)
        }
    }
}