package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.projectile.util.ExemptEntities
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.server.network.ServerPlayerEntity

class SporeAction(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler, private val shouldCancel: () -> Boolean) : IActionWithCooldown {
    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80

        eventScheduler.addEvent(TimedEvent({
            ProjectileThrower {
                val projectile = SporeBallProjectile(entity, entity.world, ExemptEntities(listOf(Entities.VOID_BLOSSOM)), 4)
                projectile.setPosition(entity.eyePos)
                ProjectileData(projectile, 0.5f, 0f)
            }.throwProjectile(target.eyePos)
        }, 60, shouldCancel = shouldCancel))

        return 100
    }
}