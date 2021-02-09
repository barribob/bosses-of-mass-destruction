package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.SideShapeType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class RiftBurst(
    val entity: LivingEntity,
    val world: ServerWorld,
    private val indicatorParticle: ParticleEffect,
    private val columnParticle: ParticleEffect,
    private val riftTime: Int,
    val eventScheduler: EventScheduler,
    val onImpact: (LivingEntity) -> Unit
) {
    fun tryPlaceRift(pos: Vec3d){
        val above = BlockPos(pos.add(VecUtils.yAxis.multiply(14.0)))
        val groundPos = findGroundBelow(world, above)
        val up = groundPos.up()
        if (up.y + 28 >= above.y && isOpenBlock(up)) {
            placeRift(up)
        }
    }

    private fun placeRift(pos: BlockPos) {
        world.spawnParticle(
            indicatorParticle,
            pos.asVec3d().add(Vec3d(0.5, 0.1, 0.5)),
            Vec3d.ZERO
        )

        eventScheduler.addEvent(TimedEvent({
            val rand = world.random
            val columnVel = VecUtils.yAxis.multiply(rand.nextDouble() + 1).multiply(0.25)
            var ticks = 0
            eventScheduler.addEvent(TimedEvent({
                val impactPos = pos.up(ticks)
                world.spawnParticle(
                    columnParticle,
                    impactPos.asVec3d().add(VecUtils.unit.multiply(0.5)).add(RandomUtils.randVec().multiply(0.25)),
                    columnVel
                )

                val box = Box(impactPos)
                val entities = world.getEntitiesByClass(LivingEntity::class.java, box, null)

                entities.forEach {
                    if (it != entity) {
                        onImpact(it)
                    }
                }

                ticks += 2
            }, 0, 7))
        }, riftTime, shouldCancel = { !isOpenBlock(pos) || !entity.isAlive }))
    }

    private fun isOpenBlock(up: BlockPos?) = world.getBlockState(up).canReplace(
        AutomaticItemPlacementContext(
            world,
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    )

    /**
     * From Maelstrom Mod ModUtils.java
     */
    private fun findGroundBelow(world: World, pos: BlockPos): BlockPos {
        for (i in pos.y downTo 1) {
            val tempPos = BlockPos(pos.x, i, pos.z)
            if (world.getBlockState(tempPos).isSideSolid(world, tempPos, Direction.UP, SideShapeType.FULL)) {
                return tempPos
            }
        }
        return BlockPos(pos.x, 0, pos.z)
    }
}