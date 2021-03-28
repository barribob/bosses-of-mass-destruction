package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.block.Blocks
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class ServerGauntletDeathHandler(val entity: GauntletEntity) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        ++entity.deathTime
        if (entity.deathTime == deathAnimationTime) {
            val destructionType = VanillaCopies.getEntityDestructionType(entity.world)
            world.createExplosion(null, entity.pos.x, entity.pos.y, entity.pos.z, 4.0f, destructionType)
            createLoot(world)
            entity.remove()
        }
    }

    private fun createLoot(world: ServerWorld) {
        for (i in 0..5) {
            val randomDir = RandomUtils.randVec().normalize()
            val length = 8 - i
            val start = entity.pos
            val end = entity.pos.add(randomDir.multiply(length.toDouble()))
            val points = length * 2
            MathUtils.lineCallback(start, end, points) { vec3d: Vec3d, point: Int ->
                val blockPos = BlockPos(vec3d)
                if (point == points - 1) world.setBlockState(blockPos, Blocks.ANCIENT_DEBRIS.defaultState)
                else world.setBlockState(blockPos, Blocks.NETHERRACK.defaultState)
            }
        }
    }

    companion object {
        const val deathAnimationTime = 40
    }
}