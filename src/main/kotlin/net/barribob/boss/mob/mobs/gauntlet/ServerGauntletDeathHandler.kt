package net.barribob.boss.mob.mobs.gauntlet

import net.barribob.boss.Mod
import net.barribob.boss.config.GauntletConfig
import net.barribob.boss.mob.utils.IEntityTick
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.block.Blocks
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class ServerGauntletDeathHandler(
    val entity: GauntletEntity,
    private val eventScheduler: EventScheduler,
    private val mobConfig: GauntletConfig
) : IEntityTick<ServerWorld> {
    override fun tick(world: ServerWorld) {
        ++entity.deathTime
        if (entity.deathTime == deathAnimationTime) {
            world.createExplosion(null, entity.pos.x, entity.pos.y, entity.pos.z, 4.0f, World.ExplosionSourceType.MOB)
            if (mobConfig.spawnAncientDebrisOnDeath) createLoot(world)
            dropExp()
            entity.remove(Entity.RemovalReason.KILLED)
        }
    }

    private fun createLoot(world: ServerWorld) {
        for (i in 0..4) {
            val randomDir = RandomUtils.randVec().normalize()
            val length = 8 - i
            val start = entity.pos
            val end = entity.pos.add(randomDir.multiply(length.toDouble()))
            val points = length * 2
            MathUtils.lineCallback(start, end, points) { vec3d: Vec3d, point: Int ->
                val blockPos = BlockPos.ofFloored(vec3d)
                if (point == points - 1) world.setBlockState(blockPos, Blocks.ANCIENT_DEBRIS.defaultState)
                else world.setBlockState(blockPos, Blocks.NETHERRACK.defaultState)
            }
        }
        val chestPos = entity.blockPos.up()
        world.setBlockState(chestPos, Blocks.CHEST.defaultState, 2)
        LootableContainerBlockEntity.setLootTable(world, entity.random, chestPos, Mod.identifier("chests/gauntlet"))
    }

    private fun dropExp() {
        val expTicks = 20
        val expPerTick = (mobConfig.experienceDrop / expTicks.toFloat()).toInt()
        val pos = entity.pos
        eventScheduler.addEvent(TimedEvent({
            VanillaCopies.awardExperience(
                expPerTick,
                pos.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).multiply(2.0)),
                entity.world
            )
        }, 0, expTicks))
    }

    companion object {
        const val deathAnimationTime = 50
    }
}