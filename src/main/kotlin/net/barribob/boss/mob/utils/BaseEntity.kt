package net.barribob.boss.mob.utils

import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.maelstrom.general.event.EventScheduler
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.jetbrains.annotations.Nullable
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.manager.AnimationFactory

abstract class BaseEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world), IAnimatable {
    private val animationFactory: AnimationFactory by lazy { AnimationFactory(this) }
    override fun getFactory(): AnimationFactory = animationFactory
    var idlePosition: Vec3d = Vec3d.ZERO // TODO: I don't actually know if this implementation works
    protected open val bossBar: ServerBossBar? = null
    protected open val damageHandler: IDamageHandler? = null
    protected open val statusHandler: IStatusHandler? = null
    protected open val clientTick: IEntityTick<ClientWorld>? = null
    protected open val serverTick: IEntityTick<ServerWorld>? = null
    protected open val trackedDataHandler: ITrackedDataHandler? = null
    protected open val statusEffectHandler: IStatusEffectFilter? = null
    protected open val moveHandler: IMoveHandler? = null
    protected open val nbtHandler: INbtHandler? = null
    protected open val deathClientTick: IEntityTick<ClientWorld>? = null
    protected open val deathServerTick: IEntityTick<ServerWorld>? = null
    protected val preTickEvents = EventScheduler()
    protected val postTickEvents = EventScheduler()

    final override fun tick() {
        preTickEvents.updateEvents()
        if (idlePosition == Vec3d.ZERO) idlePosition = pos
        val sidedWorld = world
        if (sidedWorld.isClient && sidedWorld is ClientWorld) {
            clientTick()
            clientTick?.tick(sidedWorld)
        } else if (sidedWorld is ServerWorld) {
            serverTick(sidedWorld)
            serverTick?.tick(sidedWorld)
        }
        super.tick()
        postTickEvents.updateEvents()
    }

    override fun updatePostDeath() {
        val sidedWorld = world
        if (sidedWorld.isClient && sidedWorld is ClientWorld && deathClientTick != null) {
            deathClientTick?.tick(sidedWorld)
        }
        else if(sidedWorld is ServerWorld && deathServerTick != null) {
            deathServerTick?.tick(sidedWorld)
        }
        else {
            super.updatePostDeath()
        }
    }

    open fun clientTick() {} // Todo: this may not be the best pattern to use
    open fun serverTick(serverWorld: ServerWorld) {}

    override fun tickMovement() {
        super.tickMovement()
        visibilityCache.clear()
    }

    override fun mobTick() {
        super.mobTick()
        bossBar?.percent = this.health / this.maxHealth
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        if (hasCustomName()) {
            bossBar?.name = this.displayName
        }
        nbtHandler?.fromTag(tag)
    }

    final override fun setCustomName(@Nullable name: Text?) {
        super.setCustomName(name)
        bossBar?.name = this.displayName
    }

    override fun onStartedTrackingBy(player: ServerPlayerEntity?) {
        super.onStartedTrackingBy(player)
        bossBar?.addPlayer(player)
    }

    override fun onStoppedTrackingBy(player: ServerPlayerEntity?) {
        super.onStoppedTrackingBy(player)
        bossBar?.removePlayer(player)
    }

    final override fun readCustomDataFromTag(tag: CompoundTag?) {
        super.readCustomDataFromTag(tag)
    }

    // Todo: Make handler hooks final [handleStatus, onTrackedDataSet, move, fromTag, toTag]
    override fun handleStatus(status: Byte) {
        statusHandler?.handleClientStatus(status)
        super.handleStatus(status)
    }

    override fun onTrackedDataSet(data: TrackedData<*>) {
        super.onTrackedDataSet(data)
        trackedDataHandler?.onTrackedDataSet(data)
    }

    final override fun canHaveStatusEffect(effect: StatusEffectInstance): Boolean {
        val statusEffectHandler = statusEffectHandler
        if(statusEffectHandler != null) return statusEffectHandler.canHaveStatusEffect(effect)
        return super.canHaveStatusEffect(effect)
    }

    final override fun damage(source: DamageSource, amount: Float): Boolean {
        val stats = EntityStats(this)
        val handler = damageHandler
        if (!world.isClient) {
            handler?.beforeDamage(stats, source, amount)
        }
        val result = if (handler != null) {
            handler.shouldDamage(this, source, amount) && super.damage(source, amount)
        } else {
            super.damage(source, amount)
        }
        if (!world.isClient) {
            handler?.afterDamage(stats, source, amount, result)
        }
        return result
    }

    final override fun setTarget(target: LivingEntity?) {
        if (target == null) idlePosition = pos
        super.setTarget(target)
    }

    override fun move(type: MovementType, movement: Vec3d) {
        val shouldDoDefault = moveHandler?.canMove(type, movement) == true
        if(moveHandler == null || shouldDoDefault) super.move(type, movement)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        val superTag = super.toTag(tag)
        return nbtHandler?.toTag(superTag) ?: superTag
    }
}