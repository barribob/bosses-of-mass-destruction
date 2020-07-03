package net.barribob.invasion.mob

import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.adapters.HostileEntityAdapter
import net.barribob.maelstrom.adapters.IGoal
import net.barribob.maelstrom.general.yOffset
import net.barribob.maelstrom.mob.MobUtils
import net.barribob.maelstrom.mob.server.ai.TimedAttackGoal
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class MaelstromScoutEntity(entityType: EntityType<out HostileEntity>, world: World) : HostileEntityAdapter(entityType, world) {

    override fun getAttributes(attributes: MutableList<Pair<EntityAttribute, Double>>) {
        attributes.add(Pair(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26))
        attributes.add(Pair(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0))
        attributes.add(Pair(EntityAttributes.GENERIC_MAX_HEALTH, 25.0))
    }

    override fun getGoals(goals: MutableList<Pair<Int, IGoal>>, targetGoals: MutableList<Pair<Int, IGoal>>) {
        goals.add(MobUtils.getSwimmingGoal(1, this))
        goals.add(Pair(3, TimedAttackGoal(this, 3F, 2.5F, 5, ::handleAttack)))
        goals.add(MobUtils.getWanderingGoal(4, 1.0, this))
        targetGoals.add(MobUtils.getTargetSelectGoal(1, this))
        targetGoals.add(MobUtils.getRevengeGoal(2, this))
    }

    private fun handleAttack(): Int {
        MobUtils.leapTowards(this, this.target!!.pos, 0.4, 0.0)
        MaelstromMod.serverAnimationWatcher.startAnimation(this, "scout.attack")

        MaelstromMod.serverEventScheduler.addEvent( { this.target == null || !this.isAlive || this.health <= 0 }, {
            val pos: Vec3d = this.pos.yOffset(1.0).add(this.rotationVector)
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F / (this.random.nextFloat() * 0.4F + 0.8F))
            MobUtils.handleAreaImpact(
                0.6,
                this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat(),
                this,
                pos,
                DamageSource.mob(this),
                0.20,
                damageDecay = false
            )
        }, 10)

        return 20
    }
}