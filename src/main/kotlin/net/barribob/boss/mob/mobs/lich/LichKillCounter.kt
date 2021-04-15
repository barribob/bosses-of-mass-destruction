package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ILichSummonCounter
import net.barribob.boss.cardinalComponents.IWorldEventScheduler
import net.barribob.boss.config.LichConfig
import net.barribob.boss.mob.spawn.*
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.Heightmap
import net.minecraft.world.dimension.DimensionType

class LichKillCounter(
    private val config: LichConfig.SummonMechanic,
    private val eventScheduler: IWorldEventScheduler,
    private val summonCounter: ILichSummonCounter
) : ServerEntityCombatEvents.AfterKilledOtherEntity {
    private val countedEntities = config.entitiesThatCountToSummonCounter.map { Registry.ENTITY_TYPE[Identifier(it)] }

    private fun getBarName(entitiesKilled: Int): TranslatableText {
        val identifier = Mod.identifier("kill_counter_update")
        return TranslatableText("${identifier.namespace}.${identifier.path}", entitiesKilled)
    }

    override fun afterKilledOtherEntity(sWorld: ServerWorld, entity: Entity, killedEntity: LivingEntity) {
        if (entity is ServerPlayerEntity && killedEntity.type in countedEntities) {
            val previouslySummoned = summonCounter.getLichSummons(entity)

            if(previouslySummoned == 0) {
                val entitiesKilled = getUndeadKilled(entity)

                onEntitiesKilledUpdate(entitiesKilled, entity, sWorld)
            }
        }
    }

    private fun onEntitiesKilledUpdate(
        entitiesKilled: Int,
        player: ServerPlayerEntity,
        sWorld: ServerWorld
    ) {
        if (entitiesKilled > 0 && entitiesKilled % config.numEntitiesKilledToShowCounter == 0) {
            displayCount(entitiesKilled, player, sWorld)
        }

        if (entitiesKilled >= config.numEntitiesKilledToSummonLich) {
            trySummonLich(player, sWorld)
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

    private fun trySummonLich(playerEntity: ServerPlayerEntity, sWorld: ServerWorld) {
        if (sWorld.registryManager.dimensionTypes.getOrThrow(DimensionType.OVERWORLD_REGISTRY_KEY) == sWorld.dimension) {
            val spawnPos =
                sWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlockPos(playerEntity.pos)).up(5).asVec3d()

            val compoundTag = CompoundTag()
            compoundTag.putString("id", Mod.identifier("lich").toString())

            val spawned = MobPlacementLogic(
                RangedSpawnPosition(spawnPos, 2.0, 10.0, ModRandom()),
                CompoundTagEntityProvider(compoundTag, sWorld, Mod.LOGGER),
                MobEntitySpawnPredicate(sWorld),
                SimpleMobSpawner(sWorld)
            ).tryPlacement(100)

            if (spawned) summonCounter.increment(playerEntity)
        }
    }

    private fun displayCount(
        entitiesKilled: Int,
        entity: ServerPlayerEntity,
        sWorld: ServerWorld
    ) {
        val bossBar = ServerBossBar(getBarName(entitiesKilled), BossBar.Color.BLUE, BossBar.Style.NOTCHED_12)
        bossBar.addPlayer(entity)
        bossBar.isVisible = true
        bossBar.percent = (entitiesKilled / config.numEntitiesKilledToSummonLich.toFloat()).coerceAtMost(1f)

        eventScheduler.getWorldEventScheduler(sWorld).addEvent(TimedEvent({
            bossBar.isVisible = false
        }, 100))
    }
}