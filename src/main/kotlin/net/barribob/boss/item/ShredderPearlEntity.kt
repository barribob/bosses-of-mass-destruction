package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.NetworkUtils.Companion.sendImpactPacket
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.thrown.EnderPearlEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.network.Packet
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * Sourced from [EnderPearlEntity]
 */
class ShredderPearlEntity : ThrownItemEntity {
    constructor(entityType: EntityType<out ShredderPearlEntity?>?, world: World?) : super(entityType, world)
    constructor(world: World, owner: LivingEntity) : super(Entities.SHREDDER_PEARL, owner, world)

    override fun getDefaultItem(): Item = Mod.items.shredderPearl

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        entityHitResult.entity.damage(DamageSource.thrownProjectile(this, owner), 0.0f)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (!world.isClient) {
            if(!this.isRemoved) serverCollision()
        }
    }

    private fun serverCollision() {
        teleportEntity(owner)
        discard()
        sendImpactPacket(world as ServerWorld, pos)
    }

    private fun teleportEntity(entity: Entity?) {
        if (entity is ServerPlayerEntity) {
            if (entity.networkHandler.getConnection().isOpen && entity.world === world && !entity.isSleeping) {
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

    override fun createSpawnPacket(): Packet<*> = Mod.networkUtils.createClientEntityPacket(this)

    companion object {
        private const val radius = 5.0
        private const val height = 5.0
        private val horizontalRodParticle = ClientParticleBuilder(Particles.HORIZONTAL_ROD)
            .brightness(Particles.FULL_BRIGHT)
            .color { age -> MathUtils.lerpVec(age, ModColors.LIGHT_ENDER_PEARL, ModColors.DARK_ENDER_PEARL) }
            .colorVariation(0.25)
        private val verticalRodParticle = ClientParticleBuilder(Particles.ROD)
            .brightness(Particles.FULL_BRIGHT)
            .color { age -> MathUtils.lerpVec(age, ModColors.LIGHT_ENDER_PEARL, ModColors.DARK_ENDER_PEARL) }
            .colorVariation(0.25)

        fun handleShredderPearlImpact(world: World, pos: Vec3d) {
            spawnVerticalRods(world, pos)

            for(i in 0..radius.toInt()) {
                spawnHorizontalRods(i, pos)
            }
        }

        private fun spawnVerticalRods(world: World, pos: Vec3d) {
            var y = 0
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                spawnVerticalRodRing(y, pos)
                y++
            }, 0, height.toInt()))
        }

        private fun spawnHorizontalRods(y: Int, pos: Vec3d) {
            val radius = 1 + y * ((y - 1) / radius)
            val numPoints = 10 + (y * 4)
            MathUtils.circleCallback(radius, numPoints, VecUtils.yAxis) {
                val offset = it.add(Vec3d(RandomUtils.range(-0.5, 0.5), 0.0, RandomUtils.range(-0.5, 0.5)))
                horizontalRodParticle
                    .rotation(-MathUtils.directionToYaw(offset).toFloat() + 90)
                    .build(pos.add(offset))
            }
        }

        private fun spawnVerticalRodRing(y: Int, pos: Vec3d) {
            MathUtils.circleCallback(radius, 50 - (y * 4), VecUtils.yAxis) {
                verticalRodParticle
                    .build(
                        pos.add(it).add(VecUtils.yAxis.multiply(y + RandomUtils.range(0.0, 1.0))),
                        VecUtils.yAxis.multiply(0.1))
            }
        }
    }
}
