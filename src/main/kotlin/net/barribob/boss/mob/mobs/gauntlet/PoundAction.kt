package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.VanillaCopies
import net.barribob.boss.utils.VanillaCopies.lookAtTarget
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue

class PoundAction(val entity: GauntletEntity, val eventScheduler: EventScheduler) : IActionWithCooldown {
    private var previousSpeed = 0.0

    override fun perform(): Int {
        val target = entity.target ?: return 40
        val targetPos = target.pos
        val accelerateStartTime = 16
        val unclenchTime = 60
        var doFirePunch = false

        entity.addVelocity(0.0, 0.9, 0.0)
        val accelerateHorizontally = TimedEvent(
            { accelerateToTarget(Vec3d(targetPos.x, entity.eyeY, targetPos.z)) },
            accelerateStartTime,
            15,
            { targetPos.distanceTo(Vec3d(entity.pos.x, targetPos.y, entity.pos.z)) < 3 })
        val accelerateVertically = TimedEvent(
            { accelerateToTarget(Vec3d(entity.pos.x, targetPos.y, entity.pos.z), 0.35) },
            0,
            15,
            { (entity.eyeY - targetPos.y).absoluteValue < 1 })
        eventScheduler.addEvent(
            TimedEvent(
                { whilePunchActive(doFirePunch) },
                accelerateStartTime,
                unclenchTime - accelerateStartTime
            )
        )
        eventScheduler.addEvent(
            EventSeries(
                accelerateHorizontally,
                TimedEvent(::lookDown, 0, 14),
                TimedEvent({
                    doFirePunch = true
                    lookDown()
                }, 0),
                accelerateVertically,
                TimedEvent(::lookDown, 0, 5)
            )
        )
        val closeFistAnimationTime = 7
        // Todo: fix gauntlet uncommonly exploding in midair as if hitting something
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, closeFistAnimationTime))

        eventScheduler.addEvent(TimedEvent({
            entity.hitboxHelper.setOpenHandHitbox()
            entity.world.sendEntityStatus(entity, GauntletAttacks.stopPoundAnimation)
        }, unclenchTime))

        return 100
    }

    private fun whilePunchActive(doFirePunch: Boolean) {
        testBlockPhysicalImpact(doFirePunch)
        val speed = entity.velocity.length()
//        if(speed > 0.25) entity.destroyBlocks(Box(entity.pos, entity.pos).expand(1.0).offset(0.0, -0.5, 0.0))
        previousSpeed = speed
    }

    private fun testBlockPhysicalImpact(doFirePunch: Boolean) {
        if((entity.horizontalCollision || entity.verticalCollision) && previousSpeed > 0.55f) {
            val pos: Vec3d = entity.pos
            val flag = VanillaCopies.getEntityDestructionType(entity.world)
            val size = if (doFirePunch) 4f else (previousSpeed * 1.5).toFloat()
            entity.world.createExplosion(entity, pos.x, pos.y, pos.z, size, doFirePunch, flag)
        }
    }

    private fun accelerateToTarget(target: Vec3d, speed: Double = 0.25) {
        val dir: Vec3d = MathUtils.unNormedDirection(entity.eyePos(), target).normalize()
        val velocityCorrection: Vec3d = entity.velocity.planeProject(dir)
        entity.addVelocity(dir.subtract(velocityCorrection).multiply(speed))

        lookDown()
    }

    private fun lookDown() {
        val down = entity.pos.add(entity.velocity.planeProject(VecUtils.yAxis)).subtract(VecUtils.yAxis.multiply(50.0))
        entity.lookControl.lookAt(down)
        entity.lookAtTarget(down, entity.lookYawSpeed.toFloat(), 180f)
    }
}