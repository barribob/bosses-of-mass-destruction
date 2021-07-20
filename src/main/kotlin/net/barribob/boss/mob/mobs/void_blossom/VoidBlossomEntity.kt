package net.barribob.boss.mob.mobs.void_blossom

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import io.github.stuff_stuffs.multipart_entities.common.entity.MultipartAwareEntity
import io.github.stuff_stuffs.multipart_entities.common.util.CompoundOrientedBox
import net.barribob.boss.mob.ai.goals.ActionGoal
import net.barribob.boss.mob.ai.goals.CompositeGoal
import net.barribob.boss.mob.ai.goals.FindTargetGoal
import net.barribob.boss.mob.mobs.gauntlet.AnimationHolder
import net.barribob.boss.mob.utils.BaseEntity
import net.barribob.boss.utils.AnimationUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData

class VoidBlossomEntity(entityType: EntityType<out PathAwareEntity>, world: World) : BaseEntity(entityType, world),
    MultipartAwareEntity {
    override val statusHandler = AnimationHolder(
        this, mapOf(
            Pair(VoidBlossomAttacks.spikeAttack, AnimationHolder.Animation("spike", "idle")),
            Pair(VoidBlossomAttacks.spikeWaveAttack, AnimationHolder.Animation("spike_wave", "idle")),
            Pair(VoidBlossomAttacks.sporeAttack, AnimationHolder.Animation("spore", "idle")),
            Pair(VoidBlossomAttacks.bladeAttack, AnimationHolder.Animation("leaf_blade", "idle")),
        ),
        VoidBlossomAttacks.stopAttackAnimation
    )
    override val bossBar = ServerBossBar(this.displayName, BossBar.Color.PINK, BossBar.Style.NOTCHED_12)
    val clientSpikeHandler = VoidBlossomClientSpikeHandler()
    override val clientTick = clientSpikeHandler
    override val serverTick = LightBlockPlacer(this)
    override val deathServerTick = LightBlockRemover(this)
    private val hitboxHelper = VoidBlossomHitboxes(this)

    init {
        ignoreCameraFrustum = true

        if (!world.isClient && world is ServerWorld) {
            val attackHandler = VoidBlossomAttacks(this, preTickEvents)
            goalSelector.add(2, CompositeGoal()) // Idle goal
            goalSelector.add(1, CompositeGoal(attackHandler.buildAttackGoal(), ActionGoal(::canContinueAttack, tickAction = ::lookAtTarget)))
            targetSelector.add(2, FindTargetGoal(this, PlayerEntity::class.java, { boundingBox.expand(it) }))
        }
    }

    private fun canContinueAttack() = isAlive && target != null

    private fun lookAtTarget() {
        val target = target
        if (target != null) {
            lookControl.lookAt(target.eyePos())
            lookAtEntity(target, bodyYawSpeed.toFloat(), lookPitchSpeed.toFloat())
        }
    }

    override fun registerControllers(data: AnimationData) {
        data.shouldPlayWhilePaused = true
        statusHandler.registerControllers(data)
        data.addAnimationController(AnimationController(this, "leaves", 5f, AnimationUtils.createIdlePredicate("leaves")))
    }

    override fun move(type: MovementType, movement: Vec3d) {
        super.move(type, Vec3d(0.0, movement.y, 0.0))
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun getCompoundBoundingBox(bounds: Box?): CompoundOrientedBox = hitboxHelper.getHitbox().getBox(bounds)
    override fun getBounds(): EntityBounds = hitboxHelper.getHitbox()
    override fun isInsideWall(): Boolean = false

    override fun onSetPos(x: Double, y: Double, z: Double) {
        if (hitboxHelper != null) hitboxHelper.updatePosition()
    }

    override fun setNextDamagedPart(part: String?) {
        hitboxHelper.setNextDamagedPart(part)
    }
}