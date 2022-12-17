package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.IStatusHandler
import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.core.animation.AnimatableManager
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.RawAnimation

class AnimationHolder(val entity: BaseEntity, private val animationStatusFlags: Map<Byte, Animation>, private val stopAttackByte: Byte, private val transition: Int = 5) : IStatusHandler {
    private var nextAnimation: Animation? = null
    private var doIdleAnimation = true

    fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(AnimationController(entity, transition, attack))
    }

    override fun handleClientStatus(status: Byte) {
        if (animationStatusFlags.containsKey(status)) {
            doIdleAnimation = false
            nextAnimation = animationStatusFlags[status]
        }
        if (status == stopAttackByte) doIdleAnimation = true
    }
    
    private val attack = AnimationController.AnimationStateHandler<BaseEntity> {
        val animationData = nextAnimation
        nextAnimation = null
        if (animationData != null) {
            it.controller.forceAnimationReset()
            it.controller.setAnimation(
                RawAnimation.begin().thenPlay(animationData.animationName).thenLoop(animationData.idleAnimationName)
            )
        }

        if (doIdleAnimation) {
            it.controller.setAnimation(
                RawAnimation.begin().thenLoop("idle")
            )
        }

        PlayState.CONTINUE
    }

    data class Animation(val animationName: String, val idleAnimationName: String)
}