package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class Spikes(
    val entity: LivingEntity,
    val world: ServerWorld,
    private val indicatorParticle: ParticleEffect,
    private val riftTime: Int,
    val eventScheduler: EventScheduler,
    val onImpact: (LivingEntity) -> Unit,
    private val shouldCancel: () -> Boolean
) {
    fun tryPlaceRift(pos: Vec3d): BlockPos? {
        val above = BlockPos(pos.add(VecUtils.yAxis.multiply(14.0)))
        val groundPos = world.findGroundBelow(above)
        val up = groundPos.up()
        if (up.y + 28 >= above.y && isOpenBlock(up)) {
            placeRift(up)
            return groundPos
        }

        return null
    }

    private fun placeRift(pos: BlockPos) {
        world.spawnParticle(
            indicatorParticle,
            pos.asVec3d().add(Vec3d(0.5, 0.1, 0.5)),
            Vec3d.ZERO
        )

        eventScheduler.addEvent(TimedEvent({
            val box = Box(pos).expand(.0, 4.0, .0)
            val entities = world.getEntitiesByClass(LivingEntity::class.java, box) { it != entity }

            entities.forEach {
                if (it != entity) {
                    onImpact(it)
                }
            }
        }, riftTime, shouldCancel = shouldCancel))
    }

    private fun isOpenBlock(up: BlockPos?) = world.getBlockState(up).canReplace(
        AutomaticItemPlacementContext(
            world,
            up,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    ) || world.getBlockState(up).block == Blocks.MOSS_CARPET
}