package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box

class VoidBlossomCompoundHitbox(
    private val entity: VoidBlossomEntity,
    private val hitboxes: EntityBounds,
    private val root: String,
    private val collisionHitbox: Box,
    private val spikedBoxes: List<String>
) : ICompoundHitbox, IDamageHandler, IEntityTick<ServerWorld> {
    private var nextDamagedPart: String? = null

    override fun updatePosition() {
        val rootYaw = hitboxes.getPart(root)
        rootYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)

        rootYaw.setX(entity.x)
        rootYaw.setY(entity.y)
        rootYaw.setZ(entity.z)

        val overrideBox = hitboxes.overrideBox
        if (overrideBox != null) overrideBox.box = collisionHitbox.offset(entity.pos).offset(-1.0, 0.0, -1.0)
    }

    override fun getBounds(): EntityBounds = hitboxes

    override fun setNextDamagedPart(part: String?) {
        nextDamagedPart = part
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val part = nextDamagedPart
        nextDamagedPart = null

        if (result) {
            if (spikedBoxes.contains(part)) {
                val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                damageSource.attacker?.damage(DamageSource.thorns(entity), damage)
            }
        }
    }

    override fun tick(world: ServerWorld) {
        val rootYaw = hitboxes.getPart(root)
        rootYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)
    }
}