package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.gauntlet.GauntletEntity.Companion.isEnergized
import net.barribob.boss.mob.mobs.gauntlet.PunchAction.Companion.accelerateTowardsTarget
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class SwirlPunchAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerWorld
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
        serverWorld.playSound(
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
            if (entity.dataTracker.get(isEnergized)) {
                entity.world.createExplosion(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    mobConfig.energizedPunchExplosionSize.toFloat(),
                    true,
                    World.ExplosionSourceType.MOB
                )
                entity.dataTracker.set(isEnergized, false)
            } else {
                entity.world.createExplosion(
                    entity,
                    pos.x,
                    pos.y,
                    pos.z,
                    (previousSpeed * mobConfig.normalPunchExplosionMultiplier).toFloat(),
                    World.ExplosionSourceType.MOB
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
}