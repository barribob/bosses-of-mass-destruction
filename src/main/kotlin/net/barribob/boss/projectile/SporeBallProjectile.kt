package net.barribob.boss.projectile

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.obsidilith.RiftBurst
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory
import java.util.function.Predicate

class SporeBallProjectile : BaseThrownItemEntity, IAnimatable {
    private val circlePoints = MathUtils.buildBlockCircle(7.0)
    private val projectileParticles = ClientParticleBuilder(Particles.DISAPPEARING_SWIRL)
        .color(ModColors.GREEN)
        .colorVariation(0.4)
        .scale(0.2f)
        .brightness(Particles.FULL_BRIGHT)
    private val particle: Byte = 5
    var impacted = false
        private set
    override val collisionPredicate = Predicate<HitResult> { true }
    private var impactedPitch = 0f
    var impactedTicks = 0f
        private set

    constructor(entityType: EntityType<out ThrownItemEntity>, world: World?) : super(entityType, world)

    constructor(
        livingEntity: LivingEntity,
        world: World,
        entityPredicate: Predicate<EntityHitResult>
    ) : super(
        Entities.SPORE_BALL,
        livingEntity,
        world,
        entityPredicate
    )

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        onImpact()
    }

    override fun clientTick() {
        super.clientTick()
        if (impacted) {
            impactedTicks++
        }
    }

    override fun getVelocity(): Vec3d {
        return if (!impacted) {
            super.getVelocity()
        } else {
            Vec3d.ZERO
        }
    }

    private fun onImpact() {
        if (impacted) return

        impactedPitch = pitch
        impacted = true
        val owner = owner
        if (owner != null && owner is LivingEntity) {
            doExplosion(owner)
        }
    }

    override fun getPitch() = if (impacted) impactedPitch else age * 5f
    override fun getYaw() = 0f

    private fun doExplosion(owner: LivingEntity) {
        world.sendEntityStatus(this, particle)
        playSound(Mod.sounds.sporeBallLand, 1.0f, random.randomPitch() - 0.2f)
        val eventScheduler = ModComponents.getWorldEventScheduler(world)
        val onImpact: (LivingEntity) -> Unit = {
            val damage = owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
            it.damage(DamageSource.mob(it), damage)
            it.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 140), this.owner)
        }

        val riftBurst = RiftBurst(
            owner,
            world as ServerWorld,
            Particles.SPORE_INDICATOR,
            Particles.SPORE,
            explosionDelay,
            eventScheduler,
            onImpact,
            ::isOpenBlock,
            ::posFinder
        )

        eventScheduler.addEvent(TimedEvent({
            playSound(Mod.sounds.sporeImpact, 1.5f, random.randomPitch())
            discard()
        }, explosionDelay))

        val center = blockPos.asVec3d().add(VecUtils.unit.multiply(0.5))
        for (point in circlePoints) {
            riftBurst.tryPlaceRift(center.add(point))
        }
    }

    private fun isOpenBlock(up: BlockPos?) = world.getBlockState(up).canReplace(
        AutomaticItemPlacementContext(
            world,
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    ) || world.getBlockState(up).block == Blocks.MOSS_CARPET

    private fun posFinder(pos: Vec3d): BlockPos? {
        val above = BlockPos(pos.add(VecUtils.yAxis.multiply(2.0)))
        val groundPos = world.findGroundBelow(above)
        val up = groundPos.up()
        return if (up.y + 8 >= above.y && isOpenBlock(up)) up else null
    }

    override fun handleStatus(status: Byte) {
        if (status == particle) {
            for (point in MathUtils.circlePoints(0.8, 16, VecUtils.yAxis)) {
                projectileParticles.build(point.add(pos), point.multiply(0.1))
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
                entity.damage(DamageSource.thrownProjectile(this, owner), damage)
            }
        }
    }

    override fun registerControllers(data: AnimationData) {
    }

    private val animationFactory = AnimationFactory(this)
    override fun getFactory(): AnimationFactory {
        return animationFactory
    }

    companion object {
        const val explosionDelay = 30
    }
}