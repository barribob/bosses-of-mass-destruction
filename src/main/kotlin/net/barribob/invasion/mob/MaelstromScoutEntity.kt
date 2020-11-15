package net.barribob.invasion.mob

import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.barribob.maelstrom.MaelstromMod
import net.barribob.maelstrom.general.data.BooleanFlag
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.mob.ai.TimedAttackGoal
import net.barribob.maelstrom.static_utilities.MobUtils
import net.barribob.maelstrom.static_utilities.yOffset
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData


class MaelstromScoutEntity(entityType: EntityType<out MaelstromScoutEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    private val attackFlag: BooleanFlag = BooleanFlag()
    private val attackByte: Byte = 0

    override fun initGoals() {
        super.initGoals()
        this.goalSelector.add(1, SwimGoal(this))
        this.goalSelector.add(3, TimedAttackGoal(this, 3F, 2.5F, 5, ::handleAttack))
        this.goalSelector.add(4, WanderAroundFarGoal(this, 1.0))
        this.targetSelector.add(1, MobUtils.getRevengeGoal(this))
        this.targetSelector.add(2, MobUtils.getTargetSelectGoal(this))
    }

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "idle_arms", 0f, idleArms))
        data.addAnimationController(AnimationController(this, "float", 0f, float))
        data.addAnimationController(AnimationController(this, "attack", 0f, attack))
    }

    private val idleArms = AnimationPredicate<MaelstromScoutEntity> {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation("idle_arms", true)
        )
        PlayState.CONTINUE
    }

    private val float = AnimationPredicate<MaelstromScoutEntity> {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation("float", true)
        )
        PlayState.CONTINUE
    }

    private val attack = AnimationPredicate<MaelstromScoutEntity> {
        if(attackFlag.getAndReset()) {
            it.controller.markNeedsReload()
            it.controller.setAnimation(
                AnimationBuilder()
                    .addAnimation("attack2", false)
            )
        }
        PlayState.CONTINUE
    }

    override fun handleStatus(status: Byte) {
        if(status == attackByte) attackFlag.flag()
        super.handleStatus(status)
    }

    private fun handleAttack(): Int {
        MobUtils.leapTowards(this, this.target!!.pos, 0.4, 0.0)
        world.sendEntityStatus(this, attackByte)

        val shouldCancel = { this.target == null || !this.isAlive || this.health <= 0 }
        val callback = {
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
        }
        MaelstromMod.serverEventScheduler.addEvent(
            TimedEvent(
                callback,
                10,
                shouldCancel = shouldCancel
            )
        )

        return 20
    }
}