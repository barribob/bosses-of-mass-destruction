package net.barribob.boss.mob.mobs.lich

import net.barribob.boss.Mod
import net.barribob.boss.mob.ai.action.IActionWithCooldown
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.playSound
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.StringNbtReader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

class MinionAction(
    private val entity: LichEntity,
    private val eventScheduler: EventScheduler,
    private val shouldCancel: () -> Boolean
) : IActionWithCooldown {

    override fun perform(): Int {
        val target = entity.target
        if (target !is ServerPlayerEntity) return minionSummonCooldown
        performMinionSummon(target)
        return minionSummonCooldown
    }

    private fun performMinionSummon(target: ServerPlayerEntity) {
        eventScheduler.addEvent(
            TimedEvent({ beginSummonSingleMob(target) },
                minionSummonDelay,
                shouldCancel = shouldCancel))
    }

    fun beginSummonSingleMob(target: ServerPlayerEntity) {
        val compoundTag = summonNbt.copy()
        compoundTag.putString("id", summonId)
        val mobSpawner = SimpleMobSpawner(target.serverWorld)
        val entityProvider = CompoundTagEntityProvider(compoundTag, target.serverWorld, Mod.LOGGER)
        val spawnPredicate = MobEntitySpawnPredicate(target.world)
        val summonCircleBeforeSpawn: (pos: Vec3d, entity: Entity) -> Unit = { pos, summon ->
            target.serverWorld.spawnParticle(Particles.LICH_MAGIC_CIRCLE, pos, Vec3d.ZERO)
            target.serverWorld.playSound(pos, Mod.sounds.minionRune, SoundCategory.HOSTILE, 1.0f, range = 64.0)
            eventScheduler.addEvent(
                TimedEvent({
                    mobSpawner.spawn(pos, summon)
                    entity.playSound(Mod.sounds.minionSummon, 0.7f, 1.0f)
                }, minionRuneToMinionSpawnDelay, shouldCancel = shouldCancel))
        }

        MobPlacementLogic(
            RangedSpawnPosition(target.pos, 4.0, 8.0, ModRandom()),
            entityProvider,
            spawnPredicate,
            summonCircleBeforeSpawn
        ).tryPlacement(30)
    }

    companion object {
        const val minionRuneToMinionSpawnDelay = 40
        const val minionSummonDelay = 40
        const val minionSummonParticleDelay = 10
        const val minionSummonCooldown = 80
        const val summonId = "minecraft:phantom"
        val summonNbt: NbtCompound = StringNbtReader.parse("{Health:15,Size:2,Attributes:[{Name:\"generic.max_health\",Base:15f}]}")
        val summonEntityType = Registry.ENTITY_TYPE[Identifier(summonId)]
    }
}