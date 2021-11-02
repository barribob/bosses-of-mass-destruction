package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.projectile.MagicMissileProjectile
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

class VolleyRageAction(
    private val entity: LichEntity,
    mobConfig: LichConfig,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {
    private val missileStatusEffect = Registry.STATUS_EFFECT.getOrEmpty(Identifier(mobConfig.missile.statusEffectId))
    private val missileStatusDuration = mobConfig.missile.statusEffectDuration
    private val missileStatusPotency = mobConfig.missile.statusEffectPotency
    private val missileThrower = { offset: Vec3d ->
        ProjectileThrower {
            val projectile = MagicMissileProjectile(entity, entity.world, {
                missileStatusEffect.ifPresent { effect ->
                    it.addStatusEffect(
                        StatusEffectInstance(effect,
                            missileStatusDuration,
                            missileStatusPotency)
                    )
                }
            }, listOf(MinionAction.summonEntityType))
            projectile.setPos(entity.eyePos().add(offset))
            ProjectileData(projectile, 1.6f, 0f)
        }
    }

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return 80
        return performVolley(target)
    }

    private fun performVolley(target: ServerPlayerEntity) : Int {
        val rageMissileVolleys = getRageMissileVolleys(entity).size
        target.serverWorld.playSound(entity.pos, Mod.sounds.missilePrepare, SoundCategory.HOSTILE, 4.0f, range = 64.0)
        for (i in 0 until rageMissileVolleys) {
            eventScheduler.addEvent(TimedEvent({
                val targetPos = target.boundingBox.center
                for (offset in getRageMissileVolleys(entity)[i]) {
                    missileThrower(offset).throwProjectile(targetPos.add(offset))
                }
                target.serverWorld.playSound(entity.pos, Mod.sounds.missileShoot, SoundCategory.HOSTILE, 3.0f, range = 64.0)
            },
                ragedMissileVolleyInitialDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                shouldCancel = shouldCancel))
        }
        return ragedMissileVolleyInitialDelay + (rageMissileVolleys * ragedMissileVolleyBetweenVolleyDelay)
    }

    companion object {
        const val ragedMissileVolleyInitialDelay = 60
        const val ragedMissileVolleyBetweenVolleyDelay = 30
        const val ragedMissileParticleDelay = 30

        fun getRageMissileVolleys(entity: LichEntity): List<List<Vec3d>> {
            val xOffset = 3.0
            val zOffset = 4.0
            val numPoints = 9
            val lineStart = MathUtils.axisOffset(entity.rotationVector, VecUtils.xAxis
                .multiply(xOffset)
                .add(VecUtils.zAxis.multiply(zOffset)))
            val lineEnd = MathUtils.axisOffset(entity.rotationVector, VecUtils.xAxis
                .multiply(xOffset)
                .add(VecUtils.zAxis.multiply(-zOffset)))
            val lineAcross = mutableListOf<Vec3d>()
            MathUtils.lineCallback(lineStart, lineEnd, numPoints) { v, _ -> lineAcross.add(v) }
            val lineUpDown = lineAcross.map { it.rotateVector(entity.rotationVector, 90.0) }
            val cross = lineAcross + lineUpDown
            val xVolley = cross.map { it.rotateVector(entity.rotationVector, 45.0) }

            return listOf(lineAcross, lineUpDown, cross, xVolley)
        }
    }
}