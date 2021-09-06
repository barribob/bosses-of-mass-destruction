package net.barribob.boss.block

import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class VoidLilyBlockEntity(type: BlockEntityType<*>?, pos: BlockPos?, state: BlockState?) :
    BlockEntity(type, pos, state) {
    private var structureDirection: Vec3d? = null

    override fun readNbt(nbt: NbtCompound) {
        if (nbt.contains("dirX")) {
            structureDirection = Vec3d(nbt.getDouble("dirX"), nbt.getDouble("dirY"), nbt.getDouble("dirZ"))
        }
        super.readNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val dir = structureDirection
        if (dir != null) {
            nbt.putDouble("dirX", dir.x)
            nbt.putDouble("dirY", dir.y)
            nbt.putDouble("dirZ", dir.z)
        }
        return super.writeNbt(nbt)
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: VoidLilyBlockEntity) {
            if (RandomUtils.range(0, 10) == 0 && world is ServerWorld) {
                val direction = entity.structureDirection
                if (direction == null) {
                    setNearestStructureDirection(world, pos, entity)
                }

                if (direction != null) {
                    val particlePos = pos.asVec3d().add(Vec3d(0.5, 0.7, 0.5)).add(RandomUtils.randVec().multiply(0.5))
                    world.spawnParticle(Particles.POLLEN, particlePos, direction, speed = 0.1)
                }
            }
        }

        private fun setNearestStructureDirection(
            world: ServerWorld,
            pos: BlockPos,
            entity: VoidLilyBlockEntity
        ) {
            val blockPos = world.locateStructure(ModStructures.voidBlossomArenaStructure, pos, 100, false)
            if (blockPos != null) {
                entity.structureDirection = blockPos.subtract(pos).asVec3d().normalize()
            } else {
                entity.structureDirection = VecUtils.yAxis
            }
        }
    }
}