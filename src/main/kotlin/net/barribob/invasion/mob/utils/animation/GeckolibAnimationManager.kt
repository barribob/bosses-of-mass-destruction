package net.barribob.invasion.mob.utils.animation

import net.barribob.maelstrom.general.IdProviders
import net.barribob.maelstrom.general.IdRegistry
import net.minecraft.entity.Entity
import software.bernie.geckolib.animation.controller.EntityAnimationController
import software.bernie.geckolib.entity.IAnimatedEntity
import software.bernie.geckolib.event.AnimationTestEvent
import software.bernie.geckolib.manager.EntityAnimationManager

class GeckolibAnimationManager<T>(
    val entity: T,
    val animations: IAnimationRegister<T>
) where T : Entity, T : IAnimatedEntity {
    val idRegistry = IdRegistry<EntityAnimationController<T>>(IdProviders.INCREMENT)
    val animationManager = EntityAnimationManager()

    init {
        animations.registerAnimations(this)
    }

    fun registerAnimation(
        name: String,
        animationPredicate: (EntityAnimationController<T>, AnimationTestEvent<*>?) -> Boolean
    ) {
        var controller: EntityAnimationController<T>? = null

        val animation = object : EntityAnimationController.IEntityAnimationPredicate<T> {
            override fun <E : Entity?> test(p0: AnimationTestEvent<E>?): Boolean {
                val ctrl = controller
                return if (ctrl != null) {
                    animationPredicate(ctrl, p0)
                } else {
                    false
                }
            }
        }

        // Todo: what about the animation transition length?
        controller = EntityAnimationController<T>(entity, name, 1f, animation)
        idRegistry.register(name, controller)
    }

    fun startAnimation(name: String) {
        val animationController = idRegistry.get(name)
        if (animationManager[name] == null && animationController != null) {
            animationManager.addAnimationController(animationController)
        }

        refreshAnimation(name)
    }

    private fun refreshAnimation(name: String) = animationManager[name]?.markNeedsReload()
}