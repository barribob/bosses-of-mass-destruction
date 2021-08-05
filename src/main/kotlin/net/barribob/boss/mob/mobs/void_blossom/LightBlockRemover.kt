package net.barribob.boss.mob.mobs.void_blossom

import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.maelstrom.static_utilities.MathUtils
import net.minecraft.block.Blocks
import net.minecraft.block.LightBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import kotlin.math.round

class LightBlockRemover(private val entity: LivingEntity) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        ++entity.deathTime
        val interceptedTime = MathUtils.ratioLerp(entity.deathTime.toFloat(), 0.5f, deathMaxAge, 0f) * 0.7f
        world.setBlockState(entity.blockPos, Blocks.LIGHT.defaultState.with(LightBlock.LEVEL_15, round((1 - interceptedTime) * 15).toInt()))
        if (entity.deathTime == deathMaxAge.toInt()) {
            if (world.getBlockState(entity.blockPos).block == Blocks.LIGHT) {
                world.setBlockState(entity.blockPos, Blocks.AIR.defaultState)
            }
            world.sendEntityStatus(entity, 60.toByte())
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }

    companion object {
        const val deathMaxAge = 70f
    }
}