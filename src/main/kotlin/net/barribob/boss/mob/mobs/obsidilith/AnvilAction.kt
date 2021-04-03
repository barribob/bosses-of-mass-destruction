package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.Event
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class AnvilAction(private val actor: MobEntity, val explosionPower: Float) : IActionWithCooldown {
    private val eventScheduler = ModComponents.getWorldEventScheduler(actor.world)
    private val circlePoints = MathUtils.buildBlockCircle(2.0)

    override fun perform(): Int {
        val target = actor.target
        val serverWorld = actor.world
        if (target !is LivingEntity || serverWorld !is ServerWorld) return 80
        performAttack(target, serverWorld)
        return 80
    }

    private fun performAttack(target: LivingEntity, serverWorld: ServerWorld) {
        serverWorld.playSound(actor.pos, Mod.sounds.obsidilithPrepareAttack, SoundCategory.HOSTILE, 3.0f, 1.0f, 64.0)

        eventScheduler.addEvent(TimedEvent({
            val targetPos = target.pos
            val teleportPos = targetPos.add(VecUtils.yAxis.multiply(24.0))
            val originalPos = actor.pos
            actor.refreshPositionAndAngles(teleportPos.x, teleportPos.y, teleportPos.z, actor.yaw, actor.pitch)
            serverWorld.playSound(teleportPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 3.0f, range = 64.0)

            for(pos in circlePoints) {
                val particlePos = actor.world.findGroundBelow(BlockPos(pos.add(targetPos)).up(3)).up()
                if(particlePos.y != 0) {
                    serverWorld.spawnParticle(Particles.OBSIDILITH_ANVIL_INDICATOR, particlePos.asVec3d().add(Vec3d(0.5, 0.1, 0.5)), Vec3d.ZERO)
                }
            }

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
                    serverWorld.playSound(actor.pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, range = 64.0)
                }, 20, shouldCancel = { !actor.isAlive}))
            }, shouldCancel = shouldCancelLand))

        }, 20, shouldCancel = { !actor.isAlive }))
    }
}