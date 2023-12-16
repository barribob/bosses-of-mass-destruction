package net.barribob.maelstrom.static_utilities

import net.minecraft.entity.Entity
import net.minecraft.registry.Registries
import net.minecraft.util.math.Vec3d

fun Entity.addVelocity(vec: Vec3d) {
    this.addVelocity(vec.x, vec.y, vec.z)
}

fun Entity.setPos(vec: Vec3d) {
    this.updatePosition(vec.x, vec.y, vec.z)
}

fun Entity.eyePos(): Vec3d = this.getCameraPosVec(1f)

val Entity.lastRenderPos: Vec3d
    get() = Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ)

val Entity.registryId: String
    get() = Registries.ENTITY_TYPE.getId(type).toString()