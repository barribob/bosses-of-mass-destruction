package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.ai.action.ThrowProjectileAction
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.comet.CometProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.serverWorld
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.barribob.maelstrom.static_utilities.setPos
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class CometAction(
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
        if (target !is ServerPlayerEntity) return cometThrowCooldown
        performCometThrow(target.serverWorld)
        return cometThrowCooldown
    }

    private fun performCometThrow(serverWorld: ServerWorld) {
        eventScheduler.addEvent(
            TimedEvent(
                {
                    serverWorld.playSound(
                        entity.pos,
                        Mod.sounds.cometPrepare,
                        SoundCategory.HOSTILE,
                        3.0f,
                        range = 64.0
                    )
                }, 10, shouldCancel = shouldCancel
            )
        )
        eventScheduler.addEvent(
            TimedEvent(
                {
                    ThrowProjectileAction(entity, cometThrower(getCometLaunchOffset())).perform()
                    serverWorld.playSound(entity.pos, Mod.sounds.cometShoot, SoundCategory.HOSTILE, 3.0f, range = 64.0)
                },
                cometThrowDelay,
                shouldCancel = shouldCancel
            )
        )
    }

    companion object {
        const val cometThrowDelay = 60
        const val cometParticleSummonDelay = 15
        const val cometThrowCooldown = 80
        fun getCometLaunchOffset(): Vec3d = VecUtils.yAxis.multiply(2.0)
    }
}