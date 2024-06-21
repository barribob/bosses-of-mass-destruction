package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.mobs.void_blossom.hitbox.HitboxId
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.SporeBallProjectile
import net.barribob.boss.projectile.util.ExemptEntities
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.EventSeries
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory

class SporeAction(private val entity: VoidBlossomEntity, private val eventScheduler: EventScheduler, private val shouldCancel: () -> Boolean) : IActionWithCooldown {
    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80

        eventScheduler.addEvent(
            EventSeries(
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Spore.id)
                }, 20, shouldCancel = shouldCancel),
                TimedEvent({
                    entity.dataTracker.set(VoidBlossomEntity.hitboxTrackedData, HitboxId.Idle.id)
                }, 27)
            )
        )

        eventScheduler.addEvent(TimedEvent({
            target.serverWorld.playSound(entity.pos, Mod.sounds.sporePrepare, SoundCategory.HOSTILE, 1.5f, range = 32.0)
        }, 26))

        eventScheduler.addEvent(TimedEvent({
            ProjectileThrower {
                val projectile = SporeBallProjectile(
                    entity,
                    entity.world,
                    ExemptEntities(listOf(Entities.VOID_BLOSSOM))
                )
                projectile.setPosition(entity.eyePos)
                ProjectileData(projectile, 0.75f, 0f)
            }.throwProjectile(target.eyePos)
        }, 45, shouldCancel = shouldCancel))

        return 100
    }
}