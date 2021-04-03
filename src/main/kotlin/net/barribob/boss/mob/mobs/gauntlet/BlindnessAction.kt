package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendBlindnessPacket
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.entity.ai.TargetPredicate
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Box

class BlindnessAction(
    val entity: GauntletEntity,
    val eventScheduler: EventScheduler,
    private val cancelAction: () -> Boolean,
    private val serverWorld: ServerWorld
) : IActionWithCooldown {
    override fun perform(): Int {
        serverWorld.playSound(
            entity.pos,
            Mod.sounds.gauntletCast,
            SoundCategory.HOSTILE,
            3.0f,
            1.0f,
            64.0
        )

        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setClosedFistHitbox, 10, shouldCancel = cancelAction))
        eventScheduler.addEvent(TimedEvent(entity.hitboxHelper::setOpenHandHitbox, 43))

        eventScheduler.addEvent(TimedEvent({
            val players: List<PlayerEntity> = entity.world.getPlayers(
                TargetPredicate().setBaseMaxDistance(64.0),
                entity,
                Box(entity.pos, entity.pos).expand(64.0, 32.0, 64.0)
            )

            if (players.any()) {
                entity.sendBlindnessPacket(players)
                eventScheduler.addEvent(
                    TimedEvent({
                        players.forEach {
                            it.addStatusEffect(
                                StatusEffectInstance(
                                    StatusEffects.BLINDNESS,
                                    140
                                )
                            )
                        }
                    }, 50, shouldCancel = cancelAction)
                )
            }
        }, 30, shouldCancel = cancelAction))

        return 80
    }
}