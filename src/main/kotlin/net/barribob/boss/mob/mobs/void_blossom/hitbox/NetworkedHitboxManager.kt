package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.utils.BaseEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry

class NetworkedHitboxManager(private val entity: BaseEntity, private val hitboxMap: Map<Byte, ICompoundHitbox>) :
    ICompoundHitbox {
    init {
        entity.dataTracker.startTracking(hitbox, hitboxMap.keys.first())
    }

    override fun updatePosition() {
        for (hitbox in hitboxMap.values) {
            hitbox.updatePosition()
        }
    }

    override fun getBounds(): EntityBounds = hitboxMap[entity.dataTracker[hitbox]]!!.getBounds()

    override fun setNextDamagedPart(part: String?) {
        hitboxMap[entity.dataTracker[hitbox]]!!.setNextDamagedPart(part)
    }

    companion object {
        val hitbox: TrackedData<Byte> =
            DataTracker.registerData(BaseEntity::class.java, TrackedDataHandlerRegistry.BYTE)
    }
}