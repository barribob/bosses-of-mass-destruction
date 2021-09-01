package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld

class VoidBlossomDropExpDeathTick(
    private val entity: Entity,
    private val eventScheduler: EventScheduler,
    private val exp: Int,
) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        val expTicks = 20
        val expPerTick = (exp / expTicks.toFloat()).toInt()
        val fallDirection = entity.rotationVecClient.planeProject(VecUtils.yAxis).rotateY(180f)
        val originPos = entity.pos.add(VecUtils.yAxis.multiply(2.0))

        eventScheduler.addEvent(TimedEvent({
            val pos = originPos
                .add(RandomUtils.randVec().multiply(2.0))
                .add(fallDirection.multiply(RandomUtils.double(6.0) + 6.0))

            VanillaCopies.awardExperience(
                expPerTick,
                pos,
                entity.world
            )
        }, 60, expTicks))
    }
}