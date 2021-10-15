package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.NetworkUtils.Companion.sendParticlePacket
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.sin

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
            if (RandomUtils.range(0, 30) == 0 && world is ServerWorld) {
                val direction = entity.structureDirection
                if (direction == null) {
                    setNearestStructureDirection(world, pos, entity)
                }

                if (direction != null) {
                    sendParticlePacket(world, pos, direction)
                }
            }
        }

        private fun setNearestStructureDirection(
            world: ServerWorld,
            pos: BlockPos,
            entity: VoidLilyBlockEntity
        ) {
            val blockPos = world.locateStructure(ModStructures.voidBlossomArenaStructure, pos, 50, false)
            if (blockPos != null) {
                entity.structureDirection = blockPos.subtract(pos).asVec3d().normalize()
            } else {
                entity.structureDirection = VecUtils.yAxis
            }
        }

        @Environment(EnvType.CLIENT)
        private val pollenParticles = ClientParticleBuilder(Particles.POLLEN)
            .scale { age -> sin(age * Math.PI.toFloat()) * 0.04f }
            .age(30)

        @Environment(EnvType.CLIENT)
        fun spawnVoidLilyParticles(world: World, pos: Vec3d, dir: Vec3d) {
            val streakPos = pos.add(Vec3d(0.5, 0.7, 0.5)).add(RandomUtils.randVec().multiply(0.5))
            val right = dir.crossProduct(VecUtils.yAxis).normalize()
            val sinCurve = RandomUtils.range(8.0, 11.0) * RandomUtils.randSign()
            val curveLength = RandomUtils.range(1.2, 1.6)
            val particleAmount = RandomUtils.range(7, 10)

            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                val particlePos = streakPos.add(RandomUtils.randVec().multiply(0.05))
                pollenParticles
                    .continuousPosition { p ->
                        val age = p.ageRatio
                        val forward = dir.multiply(age.toDouble() * curveLength)
                        val sinSide = right.multiply(sin(age * sinCurve) * 0.1)
                        particlePos.add(forward).add(sinSide)
                    }
                    .build(particlePos)
            }, 0, particleAmount))
        }
    }
}