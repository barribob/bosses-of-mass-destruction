package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.mob.utils.animation.AnimationPredicate
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class GauntletAnimations(val entity: GauntletEntity) : IStatusHandler {
    private var nextAnimation: Animation? = null
    private var doIdleAnimation = true
    private val animationStatusFlags = mapOf(
        Pair(GauntletAttacks.punchAttack, Animation("punch_start", "punch_loop")),
        Pair(GauntletAttacks.stopPunchAnimation, Animation("punch_stop", "idle")),
        Pair(GauntletAttacks.stopPoundAnimation, Animation("pound_stop", "idle")),
        Pair(GauntletAttacks.laserAttack, Animation("laser_eye_start", "laser_eye_loop")),
        Pair(GauntletAttacks.laserAttackStop, Animation("laser_eye_stop", "idle")),
        Pair(GauntletAttacks.swirlPunchAttack, Animation("swirl_punch", "idle")),
        Pair(GauntletAttacks.blindnessAttack, Animation("cast", "idle"))
    )

    fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(entity, "attack", 5f, attack))
    }

    override fun handleClientStatus(status: Byte) {
        if (animationStatusFlags.containsKey(status)) {
            doIdleAnimation = false
            nextAnimation = animationStatusFlags[status]
        }
        if (status == GauntletAttacks.stopAttackAnimation) doIdleAnimation = true
    }

    private val attack = AnimationPredicate<GauntletEntity> {
        val animationData = nextAnimation
        nextAnimation = null
        if (animationData != null) {
            it.controller.markNeedsReload()
            it.controller.setAnimation(
                AnimationBuilder()
                    .addAnimation(animationData.animationName, false)
                    .addAnimation(animationData.idleAnimationName, true)
            )
        }

        if (doIdleAnimation) {
            it.controller.setAnimation(
                AnimationBuilder().addAnimation("idle", true)
            )
        }

        PlayState.CONTINUE
    }

    data class Animation(val animationName: String, val idleAnimationName: String)
}