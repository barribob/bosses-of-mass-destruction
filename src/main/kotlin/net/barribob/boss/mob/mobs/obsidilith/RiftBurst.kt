package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.utils.ModUtils.findGroundBelow
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AutomaticItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class RiftBurst(
    val entity: LivingEntity,
    val world: ServerWorld,
    private val indicatorParticle: ParticleEffect,
    private val columnParticle: ParticleEffect,
    private val riftTime: Int,
    val eventScheduler: EventScheduler,
    val onImpact: (LivingEntity) -> Unit,
    val isOpenBlock: (BlockPos) -> Boolean = isOpenBlock(world),
    val posFinder: (Vec3d) -> BlockPos? = defaultPosFinder(world, isOpenBlock)
) {
    fun tryPlaceRift(pos: Vec3d){
        val placement = posFinder(pos)
        if (placement != null) {
            placeRift(placement)
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
                val entities = world.getEntitiesByClass(LivingEntity::class.java, box) { it != entity }

                entities.forEach {
                    if (it != entity) {
                        onImpact(it)
                    }
                }

                ticks += 2
            }, 0, 7))
        }, riftTime, shouldCancel = { !isOpenBlock(pos) || !entity.isAlive }))
    }
}

private fun defaultPosFinder(
    world: ServerWorld,
    isOpenBlock: (BlockPos) -> Boolean
): (Vec3d) -> BlockPos? = {
    val above = BlockPos(it.add(VecUtils.yAxis.multiply(14.0)))
    val groundPos = world.findGroundBelow(above)
    val up = groundPos.up()
    if (up.y + 28 >= above.y && isOpenBlock(up)) up else null
}

private fun isOpenBlock(world: ServerWorld): (BlockPos) -> Boolean = {
    world.getBlockState(it).canReplace(
        AutomaticItemPlacementContext(
            world,
            it,
            Direction.DOWN,
            ItemStack.EMPTY,
            Direction.UP
        )
    )
}