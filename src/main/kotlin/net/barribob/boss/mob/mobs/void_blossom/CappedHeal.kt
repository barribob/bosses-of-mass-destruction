package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.mob.utils.IEntityTick
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.world.ServerWorld

class CappedHeal(
    private val entity: MobEntity,
    private val hpMilestones: List<Float>,
    private val healingPerTick: Float
) : IEntityTick<ServerWorld> {
    private val adapter = EntityAdapter(entity)
    private val stats = EntityStats(entity)

    override fun tick(world: ServerWorld) {
        if (entity.target == null) {
            LichUtils.cappedHeal(
                adapter,
                stats,
                hpMilestones,
                healingPerTick,
                entity::heal
            )
        }
    }
}