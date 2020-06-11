package net.barribob.maelstrom.mob

import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.yOffset
import net.barribob.maelstrom.mob.server.ai.TimedAttackGoal
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.FollowTargetGoal
import net.minecraft.entity.ai.goal.RevengeGoal
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class MaelstromScoutEntity(entityType: EntityType<out HostileEntity>, world: World) : HostileEntity(entityType, world) {

    override fun getAttributes(): AttributeContainer {
        return AttributeContainer(createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0).build())
    }

    override fun initGoals() {
        goalSelector.add(1, SwimGoal(this))
        goalSelector.add(2, TimedAttackGoal(this, 3F, 2.5F, 5, ::handleAttack))
        goalSelector.add(3, WanderAroundFarGoal(this, 1.0))
        targetSelector.add(1, FollowTargetGoal(this, LivingEntity::class.java, true))
        targetSelector.add(2, RevengeGoal(this, LivingEntity::class.java))
    }

    private fun handleAttack(): Int {
        MobUtils.leapTowards(this, this.target!!.pos, 0.4, 0.3)
        MaelstromMod.serverAnimationWatcher.startAnimation(this, "scout.attack")

        MaelstromMod.serverEventScheduler.addEvent( { this.target == null || !this.isAlive || this.health <= 0 }, {
            val pos: Vec3d = this.pos.yOffset(1.0).add(this.rotationVector)
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F / (this.random.nextFloat() * 0.4F + 0.8F))
            MobUtils.handleAreaImpact(0.6, this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat() , this, pos, DamageSource.mob(this), 0.20, damageDecay = false)
        }, 10)

        return 20
    }
}