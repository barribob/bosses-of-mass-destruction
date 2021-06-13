package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.mob.utils.animation.AnimationPredicate
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class AnimationHolder(val entity: BaseEntity, private val animationStatusFlags: Map<Byte, Animation>) : IStatusHandler {
    private var nextAnimation: Animation? = null
    private var doIdleAnimation = true

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

    private val attack = AnimationPredicate<BaseEntity> {
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