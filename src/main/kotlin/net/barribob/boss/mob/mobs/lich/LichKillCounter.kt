package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.IWorldEventScheduler
import net.barribob.boss.config.LichConfig
import net.barribob.maelstrom.general.event.TimedEvent
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class LichKillCounter(
    private val lichConfig: LichConfig.SummonMechanic,
    private val eventScheduler: IWorldEventScheduler
) : ServerEntityCombatEvents.AfterKilledOtherEntity {
    private val countedEntities =
        lichConfig.entitiesThatCountToSummonCounter.map { Registry.ENTITY_TYPE[Identifier(it)] }

    private fun getBarName(entitiesKilled: Int): TranslatableText {
        val identifier = Mod.identifier("kill_counter_update")
        return TranslatableText("${identifier.namespace}.${identifier.path}", entitiesKilled)
    }

    override fun afterKilledOtherEntity(sWorld: ServerWorld, entity: Entity, killedEntity: LivingEntity) {
        if (entity is ServerPlayerEntity && killedEntity.type in countedEntities) {
            val entitiesKilled = countedEntities.fold(0) { acc, entityType ->
                acc + entity.statHandler.getStat(
                    Stats.KILLED.getOrCreateStat(
                        entityType
                    )
                )
            }

            if (entitiesKilled % 2 == 0) {
                val bossBar = ServerBossBar(getBarName(entitiesKilled), BossBar.Color.BLUE, BossBar.Style.NOTCHED_12)
                bossBar.addPlayer(entity)
                bossBar.isVisible = true
                bossBar.percent = (entitiesKilled / lichConfig.numEntitiesKilledToSummonLich.toFloat()).coerceAtMost(1f)
                eventScheduler.getWorldEventScheduler(sWorld).addEvent(TimedEvent({
                    bossBar.isVisible = false
                }, 100))
            }
        }
    }
}