package net.barribob.boss.mob.mobs.gauntlet

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.utils.NetworkUtils.Companion.changeHitbox
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.eyePos
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class GauntletHitboxes(val entity: GauntletEntity) : IDamageHandler {
    private val rootBoxPitch = "rootPitch"
    private val rootBoxYaw = "rootYaw"
    private var nextDamagedPart: String? = null
    private val eyeBox = "eye"
    private val fingersBox = "fingers"
    private val thumbBox = "thumb"
    private val pinkyBox = "pinky"
    private val hitboxes: EntityBounds = EntityBounds.builder()
        .add(rootBoxYaw).setBounds(0.0, 0.0, 0.0).build()
        .add(rootBoxPitch).setBounds(2.0, 2.6, 0.6).setOffset(0.0, 1.3, 0.0).setParent(rootBoxYaw).build()
        .add(eyeBox).setBounds(0.8, 0.8, 0.2).setOffset(0.0, 0.3, 0.4).setParent(rootBoxPitch).build()
        .add(fingersBox).setBounds(1.5, 2.0, 0.5).setOffset(0.0, 1.8, 0.5).setParent(rootBoxPitch).build()
        .add(thumbBox).setBounds(0.3, 1.6, 0.3).setOffset(1.0, 0.6, 0.7).setParent(rootBoxPitch).build()
        .add(pinkyBox).setBounds(0.25, 1.0, 0.25).setOffset(-0.9, 1.7, 0.5).setParent(rootBoxPitch).build()
        .setVoxelShapeResolution(1.5)
        .factory.create()
    private val rootFistBox = "rootFist"
    private val rootFistBoxYaw = "rootFistYaw"
    private val clampedHitboxes: EntityBounds = EntityBounds.builder()
        .add(rootFistBoxYaw).setBounds(0.0, 0.0, 0.0).build()
        .add(rootFistBox).setBounds(2.0, 1.5, 2.0).setOffset(0.0, 1.0, 0.0).setParent(rootFistBoxYaw).build()
        .setVoxelShapeResolution(1.5)
        .factory.create()
    private var currentHitbox = hitboxes

    fun setOpenHandHitbox(){
        if(!entity.world.isClient && currentHitbox != hitboxes) entity.changeHitbox(true)
        currentHitbox = hitboxes
    }

    fun setClosedFistHitbox(){
        if(!entity.world.isClient && currentHitbox != clampedHitboxes) entity.changeHitbox(false)
        currentHitbox = clampedHitboxes
    }

    fun getHitbox(): EntityBounds = currentHitbox

    init {
        hitboxes.getPart(fingersBox).setRotation(35.0, 0.0, 0.0, true)
        hitboxes.getPart(thumbBox).setRotation(30.0, 0.0, 0.0, true)
        hitboxes.getPart(pinkyBox).setRotation(35.0, 0.0, 0.0, true)
    }

    fun updatePosition() {
        val rootPitch = hitboxes.getPart(rootBoxPitch)
        val rootYaw = hitboxes.getPart(rootBoxYaw)

        rootYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)
        rootPitch.setRotation(entity.pitch.toDouble(), 0.0, 0.0, true)

        rootYaw.setX(entity.x)
        rootYaw.setY(entity.y)
        rootYaw.setZ(entity.z)

        val fistYaw = clampedHitboxes.getPart(rootFistBoxYaw)
        val fist = clampedHitboxes.getPart(rootFistBox)

        fistYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)
        fist.setRotation(entity.pitch.toDouble(), 0.0, 0.0, true)

        fistYaw.setX(entity.x)
        fistYaw.setY(entity.y)
        fistYaw.setZ(entity.z)
    }

    fun setNextDamagedPart(part: String?) {
        nextDamagedPart = part
    }

    override fun shouldDamage(actor: LivingEntity, damageSource: DamageSource, amount: Float): Boolean {
        val part = nextDamagedPart
        nextDamagedPart = null

        if (part == eyeBox || damageSource.isUnblockable) return true

        if (damageSource.isExplosive) {
            val pos = damageSource.position
            if (pos != null) {
                val explosionDirection = MathUtils.unNormedDirection(pos, entity.eyePos())
                if (!MathUtils.facingSameDirection(explosionDirection, entity.rotationVector)) {
                    return true
                }
            }
        }

        return false
    }
}