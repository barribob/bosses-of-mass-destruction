package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.mob.utils.ITrackedDataHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

/**
 * Rendering based on [GuardianEntity]
 */
class GauntletClientLaserHandler(val entity: GauntletEntity, val eventScheduler: EventScheduler) :
    IEntityTick<ClientWorld>, ITrackedDataHandler, IStatusHandler {
    private var cachedBeamTarget: LivingEntity? = null
    private val laserRenderPositions =
        HistoricalData<Pair<Vec3d, Vec3d>>(Pair(Vec3d.ZERO, Vec3d.ZERO), LaserAction.laserLagTicks)
    val laserChargeParticles = ClientParticleBuilder(Particles.SPARKLES)
        .brightness(Particles.FULL_BRIGHT)
        .color(ModColors.LASER_RED)
        .colorVariation(0.2)

    override fun tick(world: ClientWorld) {
        val beamTarget = getBeamTarget()
        if (beamTarget != null) {
            val centerBoxOffset = beamTarget.boundingBox.center.subtract(beamTarget.pos)
            laserRenderPositions.set(
                Pair(
                    beamTarget.pos.add(centerBoxOffset),
                    beamTarget.lastRenderPos.add(centerBoxOffset)
                )
            )
        } else {
            laserRenderPositions.clear()
        }
    }

    @Environment(EnvType.CLIENT)
    fun shouldRenderLaser() = laserRenderPositions.getSize() > 1

    @Environment(EnvType.CLIENT)
    fun getLaserRenderPos(): Pair<Vec3d, Vec3d> {
        val laserPos = laserRenderPositions.getAll().first()
        val newPos = LaserAction.extendLaser(entity, laserPos.first)
        val prevPos = LaserAction.extendLaser(entity, laserPos.second)
        val newResult = entity.world.raycast(
            RaycastContext(
                entity.eyePos(),
                newPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
            )
        )
        val prevResult = entity.world.raycast(
            RaycastContext(
                entity.eyePos(),
                prevPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
            )
        )
        val colNewPos = if (newResult.type == HitResult.Type.BLOCK) newResult.pos else newPos
        val colPrevPos = if (prevResult.type == HitResult.Type.BLOCK) prevResult.pos else prevPos
        return Pair(colNewPos, colPrevPos)
    }

    private fun hasBeamTarget(): Boolean = entity.dataTracker.get(LaserAction.laserTarget) as Int != 0

    private fun getBeamTarget(): LivingEntity? {
        return if (!hasBeamTarget()) {
            null
        } else if (entity.world.isClient) {
            if (this.cachedBeamTarget != null) {
                this.cachedBeamTarget
            } else {
                val entity = entity.world.getEntityById((entity.dataTracker.get(LaserAction.laserTarget) as Int))
                if (entity is LivingEntity) {
                    this.cachedBeamTarget = entity
                    this.cachedBeamTarget
                } else {
                    null
                }
            }
        } else {
            entity.target
        }
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        if (LaserAction.laserTarget == data) {
            this.cachedBeamTarget = null
        }
    }

    fun initDataTracker() {
        entity.dataTracker.startTracking(LaserAction.laserTarget, 0)
    }

    override fun handleClientStatus(status: Byte) {
        if (status == GauntletAttacks.laserAttack) {
            eventScheduler.addEvent(TimedEvent({
                val lookVec = entity.rotationVector
                for (i in 0..1) {
                    val circularOffset =
                        lookVec.crossProduct(VecUtils.yAxis).rotateVector(lookVec, RandomUtils.range(0, 359).toDouble())
                    val velocity = circularOffset.normalize().negate().multiply(0.07).add(entity.velocity.multiply(1.2))
                    val position = entity.eyePos().add(circularOffset).add(lookVec.multiply(0.5))
                    laserChargeParticles.build(position, velocity)
                }
            }, 0, 85))
        }
    }
}