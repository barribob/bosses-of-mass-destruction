package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.mob.ai.action.CooldownAction
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.manager.AnimationData

class ObsidilithEntity(entityType: EntityType<out ObsidilithEntity>, world: World) : BaseEntity(entityType, world){
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PINK, BossBar.Style.NOTCHED_12)
    var currentAttack: Byte = 0
    private val statusRegistry = mapOf(
        Pair(ObsidilithUtils.burstAttackStatus, BurstAction(this)),
        Pair(ObsidilithUtils.waveAttackStatus, WaveAction(this)),
        Pair(ObsidilithUtils.spikeAttackStatus, SpikeAction(this)),
        Pair(ObsidilithUtils.anvilAttackStatus, AnvilAction(this))
    )
    private val moveLogic = ObsidilithMoveLogic(statusRegistry, this)
    private val effectHandler = ObsidilithEffectHandler(this, eventScheduler)

    init {
        ignoreCameraFrustum = true

        if (!world.isClient) {
            goalSelector.add(1, buildAttackGoal())

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))
        }
    }

    private fun buildAttackGoal(): ActionGoal {
        val attackAction = CooldownAction(moveLogic, 80)
        val onCancel = {
            world.sendEntityStatus(this, ObsidilithUtils.stopAttackStatus)
            attackAction.stop()
        }
        return ActionGoal(
            ::canContinueAttack,
            tickAction = attackAction,
            endAction = onCancel
        )
    }

    private fun canContinueAttack() = isAlive && target != null

    override fun handleStatus(status: Byte) {
        val attackStatus = statusRegistry[status]
        if(attackStatus != null) {
            effectHandler.handleStatus(status)
            currentAttack = status
            eventScheduler.addEvent(TimedEvent({ currentAttack = 0 }, 40))
        }
        super.handleStatus(status)
    }

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

    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float): Boolean {
        return false
    }
}