package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.NetworkUtils.Companion.sendImpactPacket
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.EnderPearlEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.random.Random

/**
 * Sourced from [EnderPearlEntity]
 */
class ChargedEnderPearlEntity : ThrownItemEntity {
    constructor(entityType: EntityType<out ChargedEnderPearlEntity?>?, world: World?) : super(entityType, world)
    constructor(world: World, owner: LivingEntity) : super(Entities.CHARGED_ENDER_PEARL, owner, world)

    override fun getDefaultItem(): Item = Mod.items.chargedEnderPearl

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        entityHitResult.entity.damage(entityHitResult.entity.world.damageSources.thrown(this, owner), 0.0f)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (!world.isClient) {
            if(!this.isRemoved) serverCollision()
        }
    }

    private fun serverCollision() {
        teleportEntity(owner)
        applyStatusEffects(owner)
        sendImpactPacket(world as ServerWorld, pos)
        playSound(Mod.sounds.chargedEnderPearl, 1.0f, random.randomPitch() * 0.8f)
        discard()
    }

    private fun teleportEntity(entity: Entity?) {
        if (entity is ServerPlayerEntity) {
            if (entity.networkHandler.isConnectionOpen && entity.world === world && !entity.isSleeping) {
                if (entity.hasVehicle()) {
                    entity.requestTeleportAndDismount(this.x, this.y, this.z)
                } else {
                    entity.requestTeleport(this.x, this.y, this.z)
                }
                entity.onLanding()
            }
        } else if (entity != null) {
            entity.requestTeleport(this.x, this.y, this.z)
            entity.onLanding()
        }
    }

    private fun applyStatusEffects(entity: Entity?) {
        if(entity is LivingEntity) {
            entity.addStatusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 120, 1))
            entity.addStatusEffect(StatusEffectInstance(StatusEffects.SLOW_FALLING, 20, 0))
        }
        val entities = world
            .getOtherEntities(entity, Box(x, y, z, x, y, z).expand(radius * 2, impactHeight * 2, radius * 2))
            .filterIsInstance<LivingEntity>()
            .filter(::isInXzDistance)
            .filter(::isInYDistance)
        for(e in entities) {
            val direction = pos.subtract(e.pos)
            e.takeKnockback(0.4, direction.x, direction.z)
        }
    }

    private fun isInXzDistance(entity: LivingEntity) : Boolean {
        val xzLineToEntity = entity.pos.planeProject(VecUtils.yAxis).subtract(pos.planeProject(VecUtils.yAxis))
        val sqXzDistanceTowardEntity = xzLineToEntity.lengthSquared()
        val entityCenterInRadius = sqXzDistanceTowardEntity < radius * radius
        if(entityCenterInRadius) return true

        val xzDirectionTowardEntity = xzLineToEntity.normalize()
        val xzPointTowardEntity = xzDirectionTowardEntity.multiply(radius).add(pos)
        return entity.boundingBox.expand(0.0, 10.0, 0.0).contains(xzPointTowardEntity)
    }

    private fun isInYDistance(entity: LivingEntity) : Boolean {
        val yBottom = pos.y - 1
        val yTop = pos.y + impactHeight
        return entity.boundingBox.maxY > yBottom || entity.boundingBox.minY < yTop
    }

    override fun tick() {
        val entity = owner
        if (entity is PlayerEntity && !entity.isAlive()) {
            discard()
        } else {
            super.tick()
        }
    }

    override fun moveToWorld(destination: ServerWorld): Entity? {
        val entity = owner
        if (entity != null && entity.world.registryKey !== destination.registryKey) {
            owner = null
        }
        return super.moveToWorld(destination)
    }

    companion object {
        private const val radius = 3.0
        private const val impactHeight = 3.0
        private val verticalRodParticle = ClientParticleBuilder(Particles.ROD)
            .brightness(Particles.FULL_BRIGHT)
            .color { age -> MathUtils.lerpVec(age, ModColors.LIGHT_ENDER_PEARL, ModColors.DARK_ENDER_PEARL) }
            .colorVariation(0.25)
        private val enderParticle = ClientParticleBuilder(Particles.FLUFF)
            .brightness(Particles.FULL_BRIGHT)
            .color (ModColors.ENDER_PURPLE)
            .colorVariation(0.25)

        fun handlePearlImpact(pos: Vec3d) {
            spawnTeleportParticles(pos)
            spawnVerticalRods(pos)
        }

        private fun spawnTeleportParticles(pos: Vec3d) {
            for(i in 0..10) {
                val startingRotation = Random.nextInt(360)
                val randomRadius = RandomUtils.range(2.0, 3.0)
                val rotationSpeed = RandomUtils.range(3.0, 4.0)
                enderParticle
                    .continuousPosition { rotateAroundPos(pos, i, it.getAge(), startingRotation, randomRadius, rotationSpeed) }
                    .build(rotateAroundPos(pos, i, 0, startingRotation, randomRadius, rotationSpeed), Vec3d.ZERO)
            }
        }

        private fun rotateAroundPos(
            pos: Vec3d,
            i: Int,
            age: Int,
            startingRotation: Int,
            radius: Double,
            rotationSpeed: Double
        ): Vec3d {
            val yOffset = VecUtils.yAxis.multiply(i / 15.0)
            val xzOffset = VecUtils.xAxis.rotateY(Math.toRadians(age * rotationSpeed + startingRotation).toFloat())
            return pos.add(yOffset).add(xzOffset.multiply(radius))
        }

        private fun spawnVerticalRods(pos: Vec3d) {
            for(y in 0..impactHeight.toInt()) {
                spawnVerticalRodRing(y, pos)
            }
        }

        private fun spawnVerticalRodRing(y: Int, pos: Vec3d) {
            MathUtils.circleCallback(radius, 30 - (y * 3), VecUtils.yAxis) {
                verticalRodParticle
                    .build(
                        pos.add(it).add(VecUtils.yAxis.multiply(y + RandomUtils.range(0.0, 1.0))),
                        VecUtils.yAxis.multiply(0.1))
            }
        }
    }
}
