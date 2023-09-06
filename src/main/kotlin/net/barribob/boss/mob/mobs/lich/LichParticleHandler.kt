package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.mob.mobs.lich.CometRageAction.Companion.getRageCometOffsets
import net.barribob.boss.mob.mobs.lich.MinionAction.Companion.minionSummonDelay
import net.barribob.boss.mob.mobs.lich.MinionAction.Companion.minionSummonParticleDelay
import net.barribob.boss.mob.mobs.lich.VolleyRageAction.Companion.getRageMissileVolleys
import net.barribob.boss.mob.mobs.lich.VolleyRageAction.Companion.ragedMissileParticleDelay
import net.barribob.boss.mob.mobs.lich.VolleyRageAction.Companion.ragedMissileVolleyBetweenVolleyDelay
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.mob.utils.IStatusHandler
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d

class LichParticleHandler(val entity: LichEntity, val eventScheduler: EventScheduler): IStatusHandler, IEntityTick<ClientWorld> {
    private val summonMissileParticleBuilder = ParticleFactories.soulFlame().age(2).colorVariation(0.5)
    private val teleportParticleBuilder = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color(ModColors.TELEPORT_PURPLE)
        .age(10, 15)
        .brightness(Particles.FULL_BRIGHT)
    private val summonCometParticleBuilder = ParticleFactories.cometTrail().colorVariation(0.5)
    private val flameRingFactory = ParticleFactories.soulFlame()
        .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.WHITE.multiply(0.5)) }
        .age(0, 7)
    private val minionSummonParticleBuilder = ParticleFactories.soulFlame()
        .color(ModColors.WHITE)
    private val thresholdParticleBuilder = ParticleFactories.soulFlame()
        .age(20)
        .scale(0.5f)
    private val summonRingFactory = ParticleFactories.soulFlame()
        .color(LichUtils.blueColorFade)
        .colorVariation(0.5)
        .age(10)
    private val summonRingCompleteFactory = ParticleFactories.soulFlame()
        .color(ModColors.WHITE)
        .age(20, 30)
    private val deathParticleFactory = ParticleFactories.soulFlame()
        .color(LichUtils.blueColorFade)
        .age(40, 80)
        .colorVariation(0.5)
        .scale { 0.5f - (it * 0.3f) }
    private val idleParticles = ParticleFactories.soulFlame()
        .color(LichUtils.blueColorFade)
        .age(30, 40)
        .colorVariation(0.5)
        .scale { 0.25f - (it * 0.1f) }

    override fun tick(world: ClientWorld) {
        if (entity.random.nextDouble() > 0.9) idleParticles.build(
            entity.pos.subtract(VecUtils.yAxis).add(RandomUtils.randVec().multiply(2.0)), entity.velocity
        )
    }

    override fun handleClientStatus(status: Byte) {
        when (status) {
            LichActions.cometAttack -> cometEffect()
            LichActions.volleyAttack -> volleyEffect()
            LichActions.minionAttack -> minionEffect()
            LichActions.minionRageAttack -> minionRageEffect()
            LichActions.teleportAction -> teleportEffect()
            LichActions.endTeleport -> endTeleportEffect()
            LichActions.cometRageAttack -> cometRageEffect()
            LichActions.volleyRageAttack -> volleyRageEffect()
            LichActions.hpBelowThresholdStatus -> hpThresholdEffect()
            3.toByte() -> deathEffect()
        }
    }

    private fun cometEffect() {
        eventScheduler.addEvent(
            TimedEvent(
                { summonCometParticleBuilder.build(entity.eyePos().add(CometAction.getCometLaunchOffset())) },
                CometAction.cometParticleSummonDelay,
                CometAction.cometThrowDelay - CometAction.cometParticleSummonDelay,
                ::shouldCancelParticles
            )
        )
    }

    private fun volleyEffect() {
        eventScheduler.addEvent(
            TimedEvent(
                {
                    for (offset in VolleyAction.getMissileLaunchOffsets(entity)) {
                        summonMissileParticleBuilder.build(entity.eyePos().add(offset))
                    }
                },
                VolleyAction.missileParticleSummonDelay,
                VolleyAction.missileThrowDelay - VolleyAction.missileParticleSummonDelay,
                ::shouldCancelParticles
            )
        )
    }

    private fun minionEffect() {
        eventScheduler.addEvent(TimedEvent({
            minionSummonParticleBuilder.build(entity.eyePos()
                .add(VecUtils.yAxis.multiply(1.0))
                .add(
                    RandomUtils.randVec()
                    .planeProject(VecUtils.yAxis)
                    .normalize()
                    .multiply(entity.random.nextGaussian())),
                VecUtils.yAxis.multiply(RandomUtils.double(0.2) + 0.2))
        },
            minionSummonParticleDelay,
            minionSummonDelay - minionSummonParticleDelay,
            ::shouldCancelParticles))
    }

    private fun minionRageEffect() {
        eventScheduler.addEvent(TimedEvent({
            animatedParticleMagicCircle(3.0, 30, 12, 0f)
            animatedParticleMagicCircle(6.0, 60, 24, 120f)
            animatedParticleMagicCircle(9.0, 90, 36, 240f)
        }, 10, shouldCancel = ::shouldCancelParticles))
    }

    private fun teleportEffect() {
        eventScheduler.addEvent(
            TimedEvent(::spawnTeleportParticles,
                TeleportAction.beginTeleportParticleDelay,
                TeleportAction.teleportParticleDuration,
                ::shouldCancelParticles))
    }

    private fun endTeleportEffect() {
        eventScheduler.addEvent(
            TimedEvent(::spawnTeleportParticles, 1, TeleportAction.teleportParticleDuration, ::shouldCancelParticles))
    }

    private fun cometRageEffect() {
        val numComets = getRageCometOffsets(entity).size
        for (i in 0 until numComets) {
            eventScheduler.addEvent(TimedEvent({
                val cometOffset = getRageCometOffsets(entity)[i]
                summonCometParticleBuilder.build(cometOffset.add(entity.eyePos()))
            }, i * CometRageAction.delayBetweenRageComets, CometRageAction.initialRageCometDelay, ::shouldCancelParticles))
        }
        eventScheduler.addEvent(TimedEvent({
            MathUtils.circleCallback(3.0, 72, entity.rotationVector) {
                flameRingFactory.build(it.add(entity.eyePos()))
            }
        }, 0, CometRageAction.rageCometsMoveDuration, ::shouldCancelParticles))
    }

    private fun volleyRageEffect() {
        val numVolleys = getRageMissileVolleys(entity).size
        for (i in 0 until numVolleys) {
            eventScheduler.addEvent(
                TimedEvent(
                    {
                        for (offset in getRageMissileVolleys(entity)[i]) {
                            summonMissileParticleBuilder.build(entity.eyePos().add(offset))
                        }
                    },
                    ragedMissileParticleDelay + (i * ragedMissileVolleyBetweenVolleyDelay),
                    ragedMissileVolleyBetweenVolleyDelay,
                    ::shouldCancelParticles
                )
            )
        }
    }

    private fun hpThresholdEffect() {
        for (i in 0 until 20) {
            thresholdParticleBuilder
                .build(entity.eyePos(), RandomUtils.randVec())
        }
    }

    private fun deathEffect() {
        eventScheduler.addEvent(TimedEvent({
            for(i in 0..4) {
                deathParticleFactory.build(entity.eyePos(), RandomUtils.randVec())
            }
        }, 0, 10))
    }

    private fun spawnTeleportParticles() {
        teleportParticleBuilder.build(entity.eyePos()
            .add(RandomUtils.randVec()
                .multiply(3.0)))
    }

    private fun animatedParticleMagicCircle(radius: Double, points: Int, time: Int, rotationDegrees: Float): Vec3d? {
        val spellPos = entity.pos
        val circlePoints = MathUtils.circlePoints(radius, points, entity.rotationVector)
        val timeScale = time / points.toFloat()
        circlePoints.mapIndexed { index, off ->
            eventScheduler.addEvent(TimedEvent({
                off.rotateY(rotationDegrees)
                summonRingFactory.build(off.add(spellPos))
            }, (index * timeScale).toInt()))
        }
        eventScheduler.addEvent(TimedEvent({
            circlePoints.map { summonRingCompleteFactory.build(it.add(spellPos)) }
        }, (points * timeScale).toInt()))
        return spellPos
    }

    private fun shouldCancelParticles() = !entity.isAlive
}