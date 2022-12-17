package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.serverWorld
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class CometRageAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean,
    private val lichConfig: LichConfig
) : IActionWithCooldown {
    private val cometThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = CometProjectile(entity, entity.world, {
                entity.world.createExplosion(
                    entity,
                    it.x,
                    it.y,
                    it.z,
                    lichConfig.comet.explosionStrength,
                    World.ExplosionSourceType.MOB
                )
            }, listOf(MinionAction.summonEntityType))
            projectile.setPos(entity.eyePos().add(offset))
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return rageCometsMoveDuration
        performCometThrow(target)
        return rageCometsMoveDuration
    }

    private fun performCometThrow(target: ServerPlayerEntity) {
        for ((i, offset) in getRageCometOffsets(entity).withIndex()) {
            eventScheduler.addEvent(TimedEvent({
                val targetPos = target.boundingBox.center
                cometThrower(offset).throwProjectile(targetPos)
                target.serverWorld.playSound(entity.pos, Mod.sounds.cometShoot, SoundCategory.HOSTILE, 3.0f, range = 64.0)
            }, initialRageCometDelay + (i * delayBetweenRageComets), shouldCancel = shouldCancel))
        }
        target.serverWorld.playSound(entity.pos, Mod.sounds.ragePrepare, SoundCategory.HOSTILE, 1.0f, range = 64.0)
    }

    companion object {
        private const val numCometsDuringRage = 6
        const val initialRageCometDelay = 60
        const val delayBetweenRageComets = 30
        const val rageCometsMoveDuration = initialRageCometDelay + (numCometsDuringRage * delayBetweenRageComets)

        fun getRageCometOffsets(entity: LichEntity): List<Vec3d> {
            val offsets = mutableListOf<Vec3d>()
            MathUtils.circleCallback(3.0, numCometsDuringRage, entity.rotationVector) { offsets.add(it) }
            return offsets
        }
    }
}