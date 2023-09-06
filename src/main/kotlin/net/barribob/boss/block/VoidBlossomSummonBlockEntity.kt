package net.barribob.boss.block

import net.barribob.boss.mob.Entities
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class VoidBlossomSummonBlockEntity(type: BlockEntityType<*>?, pos: BlockPos?, state: BlockState?) : BlockEntity(type, pos, state) {
    private var age = 0

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: VoidBlossomSummonBlockEntity) {
            if(!world.isClient && entity.age % 20 == 0) {
                val playersInBox = world.getNonSpectatingEntities(PlayerEntity::class.java, Box(pos).expand(40.0)).any()
                if (playersInBox) {
                    val spawnPos = pos.asVec3d().add(Vec3d(0.5, 0.0, 0.5))
                    val boss = Entities.VOID_BLOSSOM.create(world)
                    boss!!.updatePosition(spawnPos.x, spawnPos.y, spawnPos.z)
                    world.spawnEntity(boss)
                    world.removeBlock(pos, false)
                }
            }
            entity.age++
        }
    }
}