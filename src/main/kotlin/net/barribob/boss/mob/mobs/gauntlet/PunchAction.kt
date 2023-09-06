package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.VanillaCopies.destroyBlocks
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class PunchAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerWorld
) : IActionWithCooldown {
    private var previousSpeed = 0.0

    override fun perform(): Int {
        val target = entity.target ?: return 40
        val targetPos = entity.eyePos().add(MathUtils.unNormedDirection(entity.eyePos(), target.pos).multiply(1.2))
        val accelerateStartTime = 16
        val unclenchTime = 56

        val breakBoundCenter = BlockPos.ofFloored(entity.pos.add(entity.rotationVector))
        val breakBounds = Box(breakBoundCenter.subtract(BlockPos(1, 1, 1)), breakBoundCenter.add(1, 2, 1))
        entity.destroyBlocks(breakBounds)
        entity.addVelocity(0.0, 0.7, 0.0)
        eventScheduler.addEvent(TimedEvent({
            serverWorld.playSound(
                entity.pos,
                Mod.sounds.gauntletClink,
                SoundCategory.HOSTILE,
                2.0f,
                pitch = entity.random.randomPitch() * 0.8f
            )
        }, 12, shouldCancel = cancelAction))

        var velocityStack = 0.6
        eventScheduler.addEvent(
            TimedEvent(
                {
                    entity.accelerateTowardsTarget(targetPos, velocityStack)
                    velocityStack = 0.32
                },
                accelerateStartTime,
                15,
                { entity.pos.squaredDistanceTo(targetPos) < 9 || cancelAction() })
        )
        eventScheduler.addEvent(TimedEvent(::whilePunchActive, accelerateStartTime, unclenchTime - accelerateStartTime, cancelAction))

        val closeFistAnimationTime = 7
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, closeFistAnimationTime, shouldCancel = cancelAction))

        eventScheduler.addEvent(TimedEvent({
            entity.world.sendEntityStatus(
                entity,
                GauntletAttacks.stopPunchAnimation
            )
        }, unclenchTime, shouldCancel = cancelAction))
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setOpenHandHitbox, unclenchTime + 8))

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

    private fun testEntityImpact() {
        val collidedEntities =
            entity.world.getEntitiesByClass(LivingEntity::class.java, entity.boundingBox) { it != entity }
        for (target in collidedEntities) {
            entity.tryAttack(target)
            target.addVelocity(entity.velocity.multiply(0.5))
        }
    }

    companion object {
        fun Entity.accelerateTowardsTarget(target: Vec3d, velocity: Double) {
            val dir: Vec3d = MathUtils.unNormedDirection(this.eyePos(), target).normalize()
            val velocityCorrection: Vec3d = this.velocity.planeProject(dir)
            this.addVelocity(dir.subtract(velocityCorrection).multiply(velocity))
        }
    }
}