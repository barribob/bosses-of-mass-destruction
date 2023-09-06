package net.barribob.boss.utils

import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d

interface Vec3dReceiver {
    fun clientHandler(world: ClientWorld, vec3d: Vec3d)
}