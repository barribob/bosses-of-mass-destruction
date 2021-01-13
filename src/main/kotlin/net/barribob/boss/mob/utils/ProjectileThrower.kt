package net.barribob.boss.mob.utils

import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class ProjectileThrower(val projectileProvider: () -> ProjectileData) {
    fun throwProjectile(target: Vec3d) {
        val projectileData = projectileProvider()
        val direction = target.subtract(projectileData.projectile.pos)
        val h = MathHelper.sqrt(direction.x * direction.x + direction.z * direction.z) * projectileData.gravityCompensation
        projectileData.projectile.setVelocity(direction.x, direction.y + h, direction.z, projectileData.speed, projectileData.divergence)
        projectileData.projectile.world.spawnEntity(projectileData.projectile)
    }
}

data class ProjectileData(val projectile: ProjectileEntity, val speed: Float, val divergence: Float, val gravityCompensation: Double = 0.2)