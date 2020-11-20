package net.barribob.invasion.mob.mobs

import net.barribob.invasion.mob.utils.BaseEntity
import net.barribob.invasion.mob.utils.animation.AnimationPredicate
import net.minecraft.entity.EntityType
import net.minecraft.world.World
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World) : BaseEntity(
    entityType,
    world
) {
    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "skull_float", 0f, createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this, "arms_idle", 0f, createIdlePredicate("arms_idle")))
        data.addAnimationController(AnimationController(this, "book_idle", 0f, createIdlePredicate("book_idle")))
    }

    private fun createIdlePredicate(animationName: String): AnimationPredicate<LichEntity> = AnimationPredicate {
        it.controller.setAnimation(
            AnimationBuilder()
                .addAnimation(animationName, true)
        )
        PlayState.CONTINUE
    }
}