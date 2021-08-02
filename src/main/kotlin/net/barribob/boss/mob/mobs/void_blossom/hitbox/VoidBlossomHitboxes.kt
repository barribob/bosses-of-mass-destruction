package net.barribob.boss.mob.mobs.void_blossom.hitbox

import io.github.stuff_stuffs.multipart_entities.common.entity.EntityBounds
import net.barribob.boss.mob.damage.CompositeDamageHandler
import net.barribob.boss.mob.damage.IDamageHandler
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.CompositeEntityTick
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class VoidBlossomHitboxes(entity: VoidBlossomEntity) {
    private val collisionHitbox = Box(Vec3d.ZERO, Vec3d(2.0, 8.0, 2.0))
    private val rootBoxYaw = "rootYaw"
    private val neck = "neck"
    private val flower = "flower"
    private val idleHitbox = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 5.5, 1.5).setOffset(.0, 2.75, 0.0).build()
            .add(neck).setBounds(1.0, 1.0, 3.5).setOffset(.0, 3.75, 1.25).setParent(rootBoxYaw).build()
            .add(flower).setBounds(4.0, 4.0, 1.0).setOffset(.0, 3.75, 3.5).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(neck, rootBoxYaw)
    )
    private val spikeHitbox = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 5.5, 1.5).setOffset(.0, 2.75, 0.0).build()
            .add(neck).setBounds(1.0, 1.0, 1.0).setOffset(.0, 2.25, 1.25).setParent(rootBoxYaw).build()
            .add(flower).setBounds(4.0, 4.0, 1.0).setOffset(.0, 2.0, 2.0).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(neck, rootBoxYaw)
    )
    private val petalHitbox = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 9.0, 1.0).setOffset(.0, 4.5, 0.0).build()
            .add(flower).setBounds(4.0, 1.0, 4.0).setOffset(.0, 5.0, 0.0).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(rootBoxYaw)
    )
    private val spikeHitbox1 = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 6.5, 1.0).setOffset(.0, 3.25, 0.0).build()
            .add(flower).setBounds(4.0, 1.0, 4.0).setOffset(.0, 3.75, 0.0).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(rootBoxYaw)
    )
    private val spikeHitbox2 = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 4.0, 1.0).setOffset(.0, 2.0, 0.0).build()
            .add(flower).setBounds(4.0, 1.0, 4.0).setOffset(.0, 2.5, 0.0).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(rootBoxYaw)
    )
    private val spikeHitbox3 = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.5, 3.75, 1.5).setOffset(.0, 3.75 * 0.5, 0.0).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(rootBoxYaw)
    )
    private val sporeHitbox = VoidBlossomCompoundHitbox(
        entity,
        EntityBounds.builder()
            .add(rootBoxYaw).setBounds(1.0, 8.0, 1.0).setOffset(.0, 4.0, 0.0).build()
            .add(flower).setBounds(2.0, 3.0, 3.0).setOffset(.0, 5.5, -1.0).setParent(rootBoxYaw).build()
            .overrideCollisionBox(collisionHitbox).factory.create(),
        rootBoxYaw,
        collisionHitbox,
        listOf(rootBoxYaw)
    )

    init{
        spikeHitbox.getBounds().getPart(flower).setRotation(10.0, 0.0, 0.0, true)

        idleHitbox.getBounds().getPart(neck).setRotation(-15.0, 0.0, 0.0, true)
        idleHitbox.getBounds().getPart(flower).setRotation(10.0, 0.0, 0.0, true)
    }

    private val hitboxMap: Map<Byte, ICompoundHitbox> = mapOf(
        Pair(HitboxId.Idle.id, idleHitbox),
        Pair(HitboxId.Spike.id, spikeHitbox),
        Pair(HitboxId.Petal.id, petalHitbox),
        Pair(HitboxId.SpikeWave3.id, spikeHitbox3),
        Pair(HitboxId.SpikeWave1.id, spikeHitbox1),
        Pair(HitboxId.SpikeWave2.id, spikeHitbox2),
        Pair(HitboxId.Spore.id, sporeHitbox),
    )

    fun getMap(): Map<Byte, ICompoundHitbox> = hitboxMap
    fun getTickers() = CompositeEntityTick(*hitboxMap.values.filterIsInstance<IEntityTick<ServerWorld>>().toTypedArray())
    fun getDamageHandlers() = CompositeDamageHandler(*hitboxMap.values.filterIsInstance<IDamageHandler>().toTypedArray())
}