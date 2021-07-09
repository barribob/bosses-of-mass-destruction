package net.barribob.boss.projectile

import net.barribob.boss.Mod
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.ProjectileData
import net.barribob.boss.mob.utils.ProjectileThrower
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.function.Predicate

class SporeBallProjectile : BaseThrownItemEntity {
    val ancestor: Int
    private val projectileParticles = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color(ModColors.GREEN)
        .colorVariation(0.3)
        .scale(0.1f)
        .brightness(Particles.FULL_BRIGHT)
    private val particle: Byte = 5

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world) {
        ancestor = 0
    }

    constructor(
        livingEntity: LivingEntity,
        world: World,
        entityPredicate: Predicate<EntityHitResult>,
        ancestor: Int
    ) : super(
        Entities.SPORE_BALL,
        livingEntity,
        world,
        entityPredicate
    ) {
        this.ancestor = ancestor
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)

        val owner = owner
        if (!world.isClient && ((hitResult.type != HitResult.Type.ENTITY) || (hitResult is EntityHitResult && hitResult.entity != owner))) {
            if (ancestor > 0 && owner is VoidBlossomEntity) {
                val targetDir = owner.target?.pos?.subtract(pos)?.normalize()?.multiply(0.5) ?: Vec3d.ZERO
                val projectileThrower = ProjectileThrower {
                    val projectile = SporeBallProjectile(owner, world, collisionPredicate, ancestor - 1)
                    projectile.setPos(x, y + 0.5, z)
                    ProjectileData(projectile, 0.5f, 0f)
                }

                for (i in 0 until 2) {
                    projectileThrower.throwProjectile(
                        RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().add(targetDir)
                            .add(VecUtils.yAxis).add(pos)
                    )
                }
            }
            world.sendEntityStatus(this, particle)
            playSound(SoundEvents.ENTITY_SLIME_JUMP_SMALL, 1.0f, random.randomPitch())

            discard()
        }
    }

    override fun handleStatus(status: Byte) {
        if (status == particle) {
            for (point in MathUtils.circlePoints(0.8, 16, VecUtils.yAxis)) {
                projectileParticles.build(point.add(pos))
            }
        }
        super.handleStatus(status)
    }

    override fun entityHit(entityHitResult: EntityHitResult) {
        if (world.isClient) return
        val owner = owner
        val entity = entityHitResult.entity

        if (owner is LivingEntity) {
            if (entity != owner) {
                val damage = owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                entity.damage(DamageSource.magic(this, owner), damage)
                if (entity is LivingEntity) {
                    entity.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 140), this.owner)
                }
                playSound(Mod.sounds.sporeImpact, 1.0f, random.randomPitch())
            }
        }
    }
}