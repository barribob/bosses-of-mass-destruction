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
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class BurstAction(val entity: LivingEntity) :
    IActionWithCooldown {
    private val circlePoints = MathUtils.buildBlockCircle(7)
    private val world = entity.world
    private val eventScheduler = ModComponents.getWorldEventScheduler(world)

    override fun perform(): Int {
        placeRifts()
        return 80
    }

    private fun placeRifts() {
        val riftBurst = RiftBurst(
            entity,
            world as ServerWorld,
            Particles.OBSIDILITH_BURST_INDICATOR,
            Particles.OBSIDILITH_BURST,
            burstDelay,
            eventScheduler
        ) {
            val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            it.sendVelocity(Vec3d(it.velocity.x, 1.0, it.velocity.z))
            it.damage(
                DamageSource.mob(entity),
                damage
            )
        }
        world.playSound(entity.pos, Mod.sounds.teleportPrepare, SoundCategory.HOSTILE, 1.0f, range = 64.0)

        eventScheduler.addEvent(TimedEvent({
            world.playSound(entity.pos, Mod.sounds.obsidilithBurst, SoundCategory.HOSTILE, 1.2f, range = 64.0)
        }, burstDelay, shouldCancel = { !entity.isAlive }))

        for (point in circlePoints) {
            riftBurst.tryPlaceRift(entity.pos.add(point))
        }
    }

    companion object {
        const val burstDelay = 30
    }
}