package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.spawn.ISpawnPredicate
import net.barribob.boss.mob.spawn.MobEntitySpawnPredicate
import net.barribob.boss.mob.spawn.MobPlacementLogic
import net.barribob.boss.mob.spawn.RangedSpawnPosition
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Heightmap

class TeleportAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val tooFarFromTargetDistance = 35.0
    private val tooCloseToTargetDistance = 20.0

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return teleportCooldown
        performTeleport(target)
        return teleportCooldown
    }

    fun performTeleport(target: ServerPlayerEntity) {
        val spawnPredicate = MobEntitySpawnPredicate(target.world)
        val entitySpawnPredicate = ISpawnPredicate { pos, e ->
            spawnPredicate.canSpawn(pos, e) && entity.inLineOfSight(target)
        }
        teleport(target, entitySpawnPredicate, spawnPredicate)
    }

    private fun teleport(
        target: ServerPlayerEntity,
        spawnPredicate: ISpawnPredicate,
        backupPredicate: ISpawnPredicate,
    ) {
        val mobPlacementLogic = buildTeleportLogic(target, target.pos, spawnPredicate)
        val success = mobPlacementLogic.tryPlacement(100)
        if(!success) {
            val safePos = entity.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlockPos.ofFloored(target.pos)).asVec3d()
            buildTeleportLogic(target, safePos, backupPredicate).tryPlacement(100)
        }
    }

    private fun buildTeleportLogic(
        target: ServerPlayerEntity,
        spawnPos: Vec3d,
        spawnPredicate: ISpawnPredicate
    ) = MobPlacementLogic(
        RangedSpawnPosition(spawnPos, tooCloseToTargetDistance,  tooFarFromTargetDistance, ModRandom()),
        { entity },
        spawnPredicate,
        { pos, e ->
            eventScheduler.addEvent(TimedEvent({
                target.serverWorld.playSound(entity.pos, Mod.sounds.teleportPrepare, SoundCategory.HOSTILE, 3.0f, range = 64.0)
                entity.collides = false
                eventScheduler.addEvent(TimedEvent({
                    e.requestTeleport(pos.x, pos.y, pos.z)
                    e.world.sendEntityStatus(e, LichActions.endTeleport)
                    target.serverWorld.playSound(entity.pos, Mod.sounds.lichTeleport, SoundCategory.HOSTILE, 2.0f, range = 64.0)
                    entity.collides = true
                }, teleportDelay - teleportStartSoundDelay))
            }, teleportStartSoundDelay, shouldCancel = shouldCancel))
        })

    companion object {
        private const val teleportCooldown = 80
        private const val teleportStartSoundDelay = 10
        private const val teleportDelay = 40
        const val beginTeleportParticleDelay = 15
        const val teleportParticleDuration = 10
    }
}