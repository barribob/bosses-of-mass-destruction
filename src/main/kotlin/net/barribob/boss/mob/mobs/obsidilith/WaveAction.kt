package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.NetworkUtils.Companion.sendVelocity
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d

class WaveAction(val entity: MobEntity) :
    IActionWithCooldown {
    private val riftRadius = 4
    private val circlePoints = MathUtils.buildBlockCircle(riftRadius)
    private val world = entity.world
    private val eventScheduler = ModComponents.getWorldEventScheduler(world)

    override fun perform(): Int {
        val target = entity.target
        if(target !is LivingEntity) return 80
        placeRifts(target)
        return 80
    }

    private fun placeRifts(target: LivingEntity) {
        val riftBurst = RiftBurst(
            entity,
            world as ServerWorld,
            Particles.OBSIDILITH_WAVE_INDICATOR,
            Particles.OBSIDILITH_WAVE,
            waveDelay,
            eventScheduler
        ) {
            val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            it.sendVelocity(Vec3d(it.velocity.x, 0.8, it.velocity.z))
            it.setOnFireFor(5)
            it.damage(
                DamageSource.mob(entity),
                damage
            )
        }

        world.playSound(entity.pos, Mod.sounds.teleportPrepare, SoundCategory.HOSTILE, 0.7f, range = 32.0)
        eventScheduler.addEvent(TimedEvent({
            val direction = MathUtils.unNormedDirection(entity.pos, target.pos).normalize().multiply(riftRadius.toDouble())
            val numRifts = 5
            val startRiftPos = entity.pos.add(direction)
            val endRiftPos = startRiftPos.add(direction.multiply(numRifts.toDouble() * 1.5))
            MathUtils.lineCallback(startRiftPos, endRiftPos, numRifts) { linePos, i ->
                eventScheduler.addEvent(TimedEvent({
                    world.playSound(linePos, Mod.sounds.waveIndicator, SoundCategory.HOSTILE, 0.7f, range = 32.0)
                    eventScheduler.addEvent(TimedEvent({
                        world.playSound(linePos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.2f, range = 32.0)
                    }, waveDelay, shouldCancel = { !entity.isAlive }))

                    for (point in circlePoints) {
                        riftBurst.tryPlaceRift(linePos.add(point))
                    }
                }, i * 8, shouldCancel = { !entity.isAlive }))
            }
        }, attackStartDelay, shouldCancel = { !entity.isAlive }))
    }

    companion object {
        const val waveDelay = 20
        const val attackStartDelay = 20
    }
}