package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.shieldPiercing
import net.barribob.boss.utils.NetworkUtils.Companion.sendVelocity
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class WaveAction(val entity: MobEntity) :
    IActionWithCooldown {
    private val riftRadius = 4.0
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
            eventScheduler,
            ::damageEntity
        )

        world.playSound(entity.pos, Mod.sounds.obsidilithPrepareAttack, SoundCategory.HOSTILE, 3.0f, 0.8f, 64.0)
        eventScheduler.addEvent(TimedEvent({
            val direction = MathUtils.unNormedDirection(entity.pos, target.pos).normalize().multiply(riftRadius)
            val numRifts = 5
            val startRiftPos = entity.pos.add(direction)
            val endRiftPos = startRiftPos.add(direction.multiply(numRifts.toDouble() * 1.5))
            MathUtils.lineCallback(startRiftPos, endRiftPos, numRifts) { linePos, i ->
                eventScheduler.addEvent(TimedEvent({
                    world.playSound(linePos, Mod.sounds.waveIndicator, SoundCategory.HOSTILE, 0.7f, range = 32.0)
                    eventScheduler.addEvent(TimedEvent({
                        world.playSound(linePos, Mod.sounds.obsidilithWave, SoundCategory.HOSTILE, 1.2f, range = 32.0)
                    }, waveDelay, shouldCancel = { !entity.isAlive }))

                    for (point in circlePoints) {
                        riftBurst.tryPlaceRift(linePos.add(point))
                    }
                }, i * 8, shouldCancel = { !entity.isAlive }))
            }
        }, attackStartDelay, shouldCancel = { !entity.isAlive }))
    }

    private fun damageEntity(entity: LivingEntity) {
        val damage = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
        entity.sendVelocity(Vec3d(entity.velocity.x, 0.8, entity.velocity.z))
        entity.setOnFireFor(5)
        entity.damage(entity.world.damageSources.shieldPiercing(entity.world, this.entity), damage)
    }

    companion object {
        const val waveDelay = 20
        const val attackStartDelay = 20
    }
}