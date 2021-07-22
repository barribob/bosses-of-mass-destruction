package net.barribob.boss.mob.mobs.void_blossom

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.utils.IEntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class VoidBlossomHitboxes(val entity: VoidBlossomEntity) : IDamageHandler, IEntityTick<ServerWorld> {
    private val collisionHitbox = Box(Vec3d.ZERO, Vec3d(2.0, 8.0, 2.0))
    private var nextDamagedPart: String? = null
    private val rootBoxYaw = "rootYaw"
    private val neck = "neck"
    private val flower = "flower"
    private val hitboxes: EntityBounds = EntityBounds.builder()
        .add(rootBoxYaw).setBounds(1.0, 5.5, 1.5).setOffset(.0, 2.75, -.25).build()
        .add(neck).setBounds(1.0, 1.0, 3.5).setOffset(.0, 3.75, 1.25).setParent(rootBoxYaw).build()
        .add(flower).setBounds(4.0, 4.0, 1.0).setOffset(.0, 3.75, 3.5).setParent(rootBoxYaw).build()
        .overrideCollisionBox(collisionHitbox).factory.create()

    init {
        hitboxes.getPart(neck).setRotation(-15.0, 0.0, 0.0, true)
        hitboxes.getPart(flower).setRotation(10.0, 0.0, 0.0, true)
    }

    fun getHitbox(): EntityBounds = hitboxes

    fun updatePosition(){
        val rootYaw = hitboxes.getPart(rootBoxYaw)
        rootYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)

        rootYaw.setX(entity.x)
        rootYaw.setY(entity.y)
        rootYaw.setZ(entity.z)

        val overrideBox = hitboxes.overrideBox
        if (overrideBox != null) overrideBox.box = collisionHitbox.offset(entity.pos).offset(-1.0, 0.0, -1.0)
    }

    fun setNextDamagedPart(part: String?) {
        nextDamagedPart = part
    }

    override fun afterDamage(stats: IEntityStats, damageSource: DamageSource, amount: Float, result: Boolean) {
        val part = nextDamagedPart
        nextDamagedPart = null

        if(result) {
            if(part == rootBoxYaw || part == neck) {
                val damage = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE).toFloat()
                damageSource.attacker?.damage(DamageSource.thorns(entity), damage)
            }
        }
    }

    override fun tick(world: ServerWorld) {
        val rootYaw = hitboxes.getPart(rootBoxYaw)
        rootYaw.setRotation(0.0, -entity.yaw.toDouble(), 0.0, true)
    }
}