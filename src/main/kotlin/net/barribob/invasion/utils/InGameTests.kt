package net.barribob.invasion.utils

import net.barribob.invasion.projectile.comet.CometProjectile
import net.barribob.maelstrom.static_utilities.ClientServerUtils
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.entity.LivingEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.Vec3d

object InGameTests {
    fun throwProjectile(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val projectile = CometProjectile(entity, entity.world)
            projectile.setProperties(entity, entity.pitch, entity.yaw, 0f, 1.5f, 1.0f)
            entity.world.spawnEntity(projectile)
        }
    }

    fun axisOffset(source: ServerCommandSource) {
        val entity = source.entityOrThrow
        if (entity is LivingEntity) {
            val points = mutableListOf<Vec3d>()
            for(vec in listOf(VecUtils.xAxis, VecUtils.yAxis, VecUtils.zAxis)) {
                val offset = MathUtils.axisOffset(entity.rotationVector, vec)
                val pos = entity.getCameraPosVec(0f)
                MathUtils.lineCallback(pos, offset.add(pos), 30) { v, _ -> points.add(v) }
            }
            ClientServerUtils.drawDebugPoints(points, 1, entity.pos, entity.world)
        }
    }
}