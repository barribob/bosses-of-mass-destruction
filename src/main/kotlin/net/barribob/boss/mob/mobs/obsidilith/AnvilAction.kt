package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.Event
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class AnvilAction(private val actor: MobEntity, val explosionPower: Float) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(actor.world)

    override fun perform(): Int {
        val target = actor.target
        if (target !is LivingEntity) return 80
        performAttack(target)
        return 80
    }

    private fun performAttack(target: LivingEntity) {
        actor.world.playSound(actor.pos, Mod.sounds.obsidilithPrepareAttack, SoundCategory.HOSTILE, 3.0f, 1.0f, 64.0)

        eventScheduler.addEvent(TimedEvent({
            val teleportPos = target.pos.add(VecUtils.yAxis.multiply(24.0))
            val originalPos = actor.pos
            actor.refreshPositionAndAngles(teleportPos.x, teleportPos.y, teleportPos.z, actor.yaw, actor.pitch)
            actor.world.playSound(teleportPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 3.0f, range = 64.0)

            val shouldLand = { actor.isOnGround || actor.y < 0 }
            val shouldCancelLand = { !actor.isAlive || shouldLand() }
            eventScheduler.addEvent(Event(shouldLand, {
                actor.world.createExplosion(
                    actor,
                    actor.x,
                    actor.y,
                    actor.z,
                    explosionPower,
                    VanillaCopies.getEntityDestructionType(actor.world)
                )
                eventScheduler.addEvent(TimedEvent({
                    actor.refreshPositionAndAngles(originalPos.x, originalPos.y, originalPos.z, actor.yaw, actor.pitch)
                    actor.world.playSound(actor.pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, range = 64.0)
                }, 20, shouldCancel = { !actor.isAlive}))
            }, shouldCancel = shouldCancelLand))

        }, 20, shouldCancel = { !actor.isAlive }))
    }
}