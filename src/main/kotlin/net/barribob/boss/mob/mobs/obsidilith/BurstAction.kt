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
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d

class BurstAction(val entity: LivingEntity) :
    IActionWithCooldown {
    private val circlePoints = MathUtils.buildBlockCircle(7.0)
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
            eventScheduler,
            ::damageEntity)
        world.playSound(entity.pos, Mod.sounds.obsidilithPrepareAttack, SoundCategory.HOSTILE, 3.0f, 0.7f, 64.0)

        eventScheduler.addEvent(TimedEvent({
            world.playSound(entity.pos, Mod.sounds.obsidilithBurst, SoundCategory.HOSTILE, 1.2f, range = 64.0)
        }, burstDelay, shouldCancel = { !entity.isAlive }))

        for (point in circlePoints) {
            riftBurst.tryPlaceRift(entity.pos.add(point))
        }
    }

    private fun damageEntity(livingEntity: LivingEntity) {
        val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
        livingEntity.sendVelocity(Vec3d(livingEntity.velocity.x, 1.3, livingEntity.velocity.z))
        livingEntity.damage(
            world.damageSources.shieldPiercing(world, entity),
            damage
        )
    }

    companion object {
        const val burstDelay = 30
    }
}