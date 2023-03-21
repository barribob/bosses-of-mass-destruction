package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.serverWorld
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory

class SpikeAction(val entity: MobEntity) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(entity.world)
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        placeSpikes(target)
        return 100
    }

    private fun placeSpikes(target: ServerPlayerEntity) {
        val riftTime = 20
        val riftBurst = RiftBurst(
            entity,
            target.serverWorld,
            Particles.OBSIDILITH_SPIKE_INDICATOR,
            Particles.OBSIDILITH_SPIKE,
            riftTime,
            eventScheduler,
            ::damageEntity
        )

        target.serverWorld.playSound(entity.pos, Mod.sounds.obsidilithPrepareAttack, SoundCategory.HOSTILE, 3.0f, 1.2f, 64.0)

        for (i in 0 until 3) {
            val timeBetweenRifts = 30
            val initialDelay = 30
            eventScheduler.addEvent(TimedEvent({
                val placement =
                    ObsidilithUtils.approximatePlayerNextPosition(ModComponents.getPlayerPositions(target), target.pos)
                target.serverWorld.playSound(placement, Mod.sounds.spikeIndicator, SoundCategory.HOSTILE, 1.0f, range = 32.0)

                eventScheduler.addEvent(TimedEvent({
                    target.serverWorld.playSound(
                        placement,
                        Mod.sounds.spike,
                        SoundCategory.HOSTILE,
                        1.2f,
                        range = 32.0
                    )
                }, riftTime, shouldCancel = { !entity.isAlive }))

                for (point in circlePoints) {
                    riftBurst.tryPlaceRift(placement.add(point))
                }
            }, initialDelay + i * timeBetweenRifts, shouldCancel = { !entity.isAlive }))
        }
    }

    private fun damageEntity(entity: LivingEntity) {
        val damage = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
        entity.damage(entity.world.damageSources.shieldPiercing(entity.world, this.entity), damage)
        entity.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2))
    }
}