package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.PetalBladeProjectile
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.rotateVector
import net.minecraft.server.network.ServerPlayerEntity

class BladeAction(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler, private val shouldCancel: () -> Boolean) : IActionWithCooldown {
    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        val projectileThrower = ProjectileThrower {
            val projectile = PetalBladeProjectile(entity, entity.world, {}, listOf(entity.type))
            projectile.setPosition(entity.eyePos)
            projectile.setNoGravity(true)
            ProjectileData(projectile, 0.9f, 0f, 0.0)
        }

        val thrower = {
            val eyePos = target.boundingBox.center
            val dir = entity.eyePos.subtract(eyePos)
            val left = dir.crossProduct(VecUtils.yAxis).normalize()
            val angled = left.rotateVector(dir, RandomUtils.randSign() * 20.0)
            val lineStart = eyePos.add(angled.multiply(7.0))
            val lineEnd = eyePos.add(angled.multiply(-7.0))
            MathUtils.lineCallback(lineStart, lineEnd, 10) { vec3d, _ ->
                projectileThrower.throwProjectile(vec3d)
            }
        }

        eventScheduler.addEvent(TimedEvent(thrower, 28, shouldCancel = shouldCancel))
        eventScheduler.addEvent(TimedEvent(thrower, 46, shouldCancel = shouldCancel))
        eventScheduler.addEvent(TimedEvent(thrower, 68, shouldCancel = shouldCancel))

        return 100
    }
}