package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.gauntlet.PunchAction.Companion.accelerateTowardsTarget
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.addVelocity
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class SwirlPunchAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig,
    private val cancelAction: () -> Boolean
) : IActionWithCooldown {
    private var previousSpeed = 0.0

    override fun perform(): Int {
        val target = entity.target ?: return 40
        val targetDirection = MathUtils.unNormedDirection(entity.eyePos(), target.boundingBox.center)
        val targetPos = entity.eyePos().add(targetDirection.multiply(1.2))
        val accelerateStartTime = 30
        val unclenchTime = 60
        val closeFistAnimationTime = 7

        entity.addVelocity(0.0, 0.7, 0.0)
        entity.world.playSound(
            entity.pos,
            Mod.sounds.gauntletSpinPunch,
            SoundCategory.HOSTILE,
            2.0f,
            1.0f,
            64.0
        )
        entity.dataTracker.set(isEnergized, true)
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, closeFistAnimationTime, shouldCancel = cancelAction))

        var velocityStack = 0.6
        eventScheduler.addEvent(
            TimedEvent(
                {
                    entity.accelerateTowardsTarget(targetPos, velocityStack)
                    velocityStack = 0.40
                },
                accelerateStartTime,
                15,
                { entity.pos.squaredDistanceTo(targetPos) < 9 || cancelAction() })
        )
        eventScheduler.addEvent(TimedEvent(::whilePunchActive, accelerateStartTime, unclenchTime - accelerateStartTime, cancelAction))
        eventScheduler.addEvent(TimedEvent({
            entity.hitboxHelper.setOpenHandHitbox()
            entity.dataTracker.set(isEnergized, false)
        }, unclenchTime))

        return 80
    }

    private fun whilePunchActive() {
        testBlockPhysicalImpact()
        testEntityImpact()
        val speed = entity.velocity.length()
        previousSpeed = speed
    }

    private fun testBlockPhysicalImpact() {
        if ((entity.horizontalCollision || entity.verticalCollision) && previousSpeed > 0.55f) {
            val pos: Vec3d = entity.pos
            val flag = VanillaCopies.getEntityDestructionType(entity.world)
            if (entity.dataTracker.get(isEnergized)) {
                entity.world.createExplosion(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    mobConfig.energizedPunchExplosionSize.toFloat(),
                    true,
                    flag
                )
                entity.dataTracker.set(isEnergized, false)
            } else {
                entity.world.createExplosion(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    (previousSpeed * mobConfig.normalPunchExplosionMultiplier).toFloat(),
                    flag
                )
            }
        }
    }

    private fun testEntityImpact() {
        val collidedEntities = entity.world.getEntitiesByClass(LivingEntity::class.java, entity.boundingBox) { it != entity }
        for (target in collidedEntities) {
            entity.tryAttack(target)
            target.addVelocity(entity.velocity.multiply(0.5))
        }
    }

    companion object {
        val isEnergized: TrackedData<Boolean> =
            DataTracker.registerData(GauntletEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
    }
}