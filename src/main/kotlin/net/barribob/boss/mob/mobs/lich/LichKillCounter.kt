package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.config.LichConfig
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.VecUtils
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier

class LichKillCounter(private val config: LichConfig.SummonMechanic) : ServerEntityCombatEvents.AfterKilledOtherEntity {
    private val countedEntities = config.entitiesThatCountToSummonCounter?.map { Registries.ENTITY_TYPE[Identifier(it)] } ?: listOf()

    override fun afterKilledOtherEntity(sWorld: ServerWorld, entity: Entity, killedEntity: LivingEntity) {
        if (entity is ServerPlayerEntity && killedEntity.type in countedEntities) {
            val entitiesKilled = getUndeadKilled(entity)
            if (entitiesKilled > 0 && entitiesKilled % config.numEntitiesKilledToDropSoulStar == 0) {
                sWorld.spawnParticle(Particles.SOUL_FLAME, killedEntity.pos.add(VecUtils.yAxis), VecUtils.unit, 15)
                killedEntity.dropItem(Mod.items.soulStar)
            }
        }
    }

    private fun getUndeadKilled(entity: ServerPlayerEntity): Int =
        countedEntities.fold(0) { acc, entityType ->
            acc + entity.statHandler.getStat(
                Stats.KILLED.getOrCreateStat(
                    entityType
                )
            )
        }
}