package net.barribob.boss.item

import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class WallTeleport(private val world: ServerWorld, private val entity: Entity) {
    private val startRange = 3.0
    private val endRange = 16.0

    fun tryTeleport(direction: Vec3d, position: Vec3d, action: (BlockPos) -> Unit = ::teleportTo): Boolean {
        val context = Context(direction, position)
        val teleportStart = getTeleportStart(context)
        if (teleportStart != null) {
            val teleportEnd = getTeleportEnd(context, teleportStart)
            if (teleportEnd != null) {
                action(teleportEnd)
                return true
            }
        }

        return false
    }

    private fun getTeleportStart(context: Context): BlockPos? {
        val startPos = BlockPos(context.position)
        val endPos = BlockPos(context.position.add(context.direction.multiply(startRange)))
        val blocksToCheck = MathUtils.getBlocksInLine(startPos, endPos)
        for (pos in blocksToCheck) {
            if (world.getBlockState(pos).isSolidBlock(world, pos)) {
                return pos
            }
        }

        return null
    }

    private fun getTeleportEnd(context: Context, startPos: BlockPos): BlockPos? {
        val endPos = startPos.add(BlockPos(context.direction.multiply(endRange)))
        val blocksToCheck = MathUtils.getBlocksInLine(startPos, endPos)
        for (pos in blocksToCheck) {
            val blockState = world.getBlockState(pos)
            if (blockState.isAir && world.getBlockState(pos.up()).isAir) {
                return pos
            }

            if (blockState.block.hardness < 0) {
                return null
            }
        }

        return null
    }

    private fun teleportTo(teleportPos: BlockPos) {
        val pos = teleportPos.asVec3d().add(Vec3d(0.5, 0.0, 0.5))
        entity.requestTeleport(pos.x, pos.y, pos.z)
    }

    private data class Context(val direction: Vec3d, val position: Vec3d)
}