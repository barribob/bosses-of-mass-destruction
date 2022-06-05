package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.DamageMemory
import net.barribob.boss.mob.damage.DamagedAttackerNotSeen
import net.barribob.boss.mob.damage.StagedDamageHandler
import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.mobs.lich.LichActions.Companion.hpBelowThresholdStatus
import net.barribob.boss.mob.mobs.lich.LichUtils.hpPercentRageModes
import net.barribob.boss.mob.mobs.void_blossom.CappedHeal
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.mob.utils.CompositeEntityTick
import net.barribob.boss.mob.utils.CompositeStatusHandler
import net.barribob.boss.utils.AnimationUtils
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.data.HistoricalData
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityGroup
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.SwimGoal
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.PhantomEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class LichEntity(entityType: EntityType<out LichEntity>, world: World, private val mobConfig: LichConfig) : BaseEntity(
    entityType,
    world
) {
    private val animationHolder = AnimationHolder(
        this, mapOf(
            Pair(LichActions.endTeleport, AnimationHolder.Animation("unteleport", "idle")),
            Pair(LichActions.cometRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.volleyRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.cometAttack, AnimationHolder.Animation("summon_fireball", "idle")),
            Pair(LichActions.minionAttack, AnimationHolder.Animation("summon_minions", "idle")),
            Pair(LichActions.minionRageAttack, AnimationHolder.Animation("rage_mode", "idle")),
            Pair(LichActions.teleportAction, AnimationHolder.Animation("teleport", "teleporting")),
            Pair(LichActions.volleyAttack, AnimationHolder.Animation("summon_missiles", "idle")),
            Pair(3, AnimationHolder.Animation("idle", "idle"))
        ),
        LichActions.stopAttackAnimation, 0f
    )
    private val minionAction = MinionAction(this, preTickEvents, ::cancelAttackAction)
    private val teleportAction = TeleportAction(this, preTickEvents, ::cancelAttackAction)
    private val statusRegistry = mapOf(
        Pair(LichActions.cometAttack, CometAction(this, preTickEvents, ::cancelAttackAction, mobConfig)),
        Pair(LichActions.volleyAttack, VolleyAction(this, mobConfig, preTickEvents, ::cancelAttackAction)),
        Pair(LichActions.minionAttack, minionAction),
        Pair(LichActions.minionRageAttack, MinionRageAction(this, preTickEvents, ::cancelAttackAction, minionAction)),
        Pair(LichActions.teleportAction, teleportAction),
        Pair(LichActions.cometRageAttack, CometRageAction(this, preTickEvents, ::cancelAttackAction, mobConfig)),
        Pair(LichActions.volleyRageAttack, VolleyRageAction(this, mobConfig, preTickEvents, ::cancelAttackAction)),
    )
    private val damageMemory = DamageMemory(5, this)
    private val moveLogic = LichMoveLogic(statusRegistry, this, damageMemory)
    private val lichParticles = LichParticleHandler(this, preTickEvents)

    val shouldSetToNighttime = mobConfig.eternalNighttime
    val velocityHistory = HistoricalData(Vec3d.ZERO)
    var collides = true

    private val cappedHeal = CappedHeal(this, hpPercentRageModes, mobConfig.idleHealingPerTick)

    override val statusHandler = CompositeStatusHandler(animationHolder, lichParticles)
    override val damageHandler = CompositeDamageHandler(
        StagedDamageHandler(hpPercentRageModes) { world.sendEntityStatus(this, hpBelowThresholdStatus) },
        DamagedAttackerNotSeen(this) { if(it is ServerPlayerEntity) teleportAction.performTeleport(it) },
        moveLogic, damageMemory )
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.BLUE, BossBar.Style.PROGRESS)
    override val serverTick = CompositeEntityTick(cappedHeal, moveLogic)
    override val clientTick = lichParticles

    init {
        ignoreCameraFrustum = true

        if (!world.isClient) {
            val attackHelper = LichActions(this, moveLogic)
            val moveHelper = LichMovement(this)

            goalSelector.add(1, SwimGoal(this))
            goalSelector.add(3, CompositeGoal(moveHelper.buildAttackMovement(), attackHelper.buildAttackGoal()))
            goalSelector.add(4, moveHelper.buildWanderGoal())

            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { this.boundingBox.expand(it) }))
        }
    }

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        animationHolder.registerControllers(data)
        data.addAnimationController(AnimationController(this, "skull_float", 0f, AnimationUtils.createIdlePredicate("skull_float")))
        data.addAnimationController(AnimationController(this, "float", 0f, AnimationUtils.createIdlePredicate("float")))
        data.addAnimationController(AnimationController(this, "book_idle", 0f, AnimationUtils.createIdlePredicate("book_idle")))
    }

    fun inLineOfSight(target: Entity) : Boolean {
        val hasDirectLineOfSight = VanillaCopies.hasDirectLineOfSight(eyePos, target.eyePos(), world, this)
        val directionToLich = MathUtils.unNormedDirection(target.eyePos(), eyePos)
        val facingSameDirection = MathUtils.facingSameDirection(target.rotationVector, directionToLich)
        return hasDirectLineOfSight && facingSameDirection
    }

    override fun clientTick() {
        velocityHistory.set(velocity)
    }

    override fun serverTick(serverWorld: ServerWorld) {
        if (shouldSetToNighttime) {
            serverWorld.timeOfDay = LichUtils.timeToNighttime(serverWorld.timeOfDay)
        }
    }

    override fun collides(): Boolean = collides
    override fun handleFallDamage(fallDistance: Float, damageMultiplier: Float, damageSource: DamageSource?) = false
    override fun getGroup(): EntityGroup = EntityGroup.UNDEAD
    override fun checkDespawn() = ModUtils.preventDespawnExceptPeaceful(this, world)
    override fun getHurtSound(source: DamageSource) = Mod.sounds.lichHurt
    override fun getDeathSound(): SoundEvent = Mod.sounds.lichDeath
    override fun getSoundVolume(): Float = 5.0f
    override fun isClimbing(): Boolean = false
    private fun cancelAttackAction() = isDead || target == null

    override fun onDeath(source: DamageSource?) {
        val expTicks = 18
        val expPerTick = (mobConfig.experienceDrop / expTicks.toFloat()).toInt()
        preTickEvents.addEvent(TimedEvent({
            VanillaCopies.awardExperience(expPerTick, eyePos(), world)
        }, 0, expTicks))

        world.getEntitiesByType(EntityType.PHANTOM, Box(blockPos).expand(100.0, 100.0, 100.0)) { true }.forEach(PhantomEntity::kill)

        super.onDeath(source)
    }

    override fun fall(
        heightDifference: Double,
        onGround: Boolean,
        landedState: BlockState?,
        landedPosition: BlockPos?,
    ) {
    }

    override fun travel(movementInput: Vec3d) {
        VanillaCopies.travel(movementInput, this)
    }
}