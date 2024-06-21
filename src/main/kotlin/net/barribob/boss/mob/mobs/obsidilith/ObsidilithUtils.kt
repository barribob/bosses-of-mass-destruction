package net.barribob.boss.mob.mobs.obsidilith

import net.barribob.boss.Mod
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities.OBSIDILITH
import net.barribob.boss.render.NodeBossBarRenderer
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.VanillaCopies
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.planeProject
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.inventory.LootableInventory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.pow

object ObsidilithUtils {
    private const val textureSize = 256
    private val bossBarDividerTexture = Mod.identifier("textures/gui/obsidilith_boss_bar_dividers.png")
    const val deathStatus: Byte = 3
    const val burstAttackStatus: Byte = 5
    const val waveAttackStatus: Byte = 6
    const val spikeAttackStatus: Byte = 7
    const val anvilAttackStatus: Byte = 8
    const val pillarDefenseStatus: Byte = 9
    val hpPillarShieldMilestones = listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f)
    val obsidilithBossBarRenderer =
        NodeBossBarRenderer(OBSIDILITH.translationKey, hpPillarShieldMilestones, bossBarDividerTexture, textureSize)
    val circlePos = MathUtils.buildBlockCircle((2.0.pow(2) + 1.0.pow(2)).pow(0.5))
    const val deathPillarHeight = 15
    const val ticksBetweenPillarLayer = 5

    fun approximatePlayerNextPosition(previousPosition: List<Vec3d>, currentPos: Vec3d): Vec3d {
        return previousPosition
            .map { it.subtract(currentPos).planeProject(VecUtils.yAxis) }
            .reduce { acc, vec3d -> acc.add(vec3d) }
            .multiply(-0.5).add(currentPos)
    }

    fun onDeath(actor: LivingEntity, experienceDrop: Int) {
        val world = actor.world
        if (!world.isClient) {
            val blockPos = actor.blockPos
            val vecPos = actor.pos
            val eventScheduler = ModComponents.getWorldEventScheduler(world)
            world.createExplosion(actor, actor.x, actor.y, actor.z, 2.0f, World.ExplosionSourceType.MOB)

            for (y in 0..deathPillarHeight) {
                eventScheduler.addEvent(TimedEvent({
                    actor.playSound(SoundEvents.BLOCK_STONE_PLACE, 1.0f, actor.random.randomPitch())
                    for (pos in circlePos) {
                        world.setBlockState(
                            BlockPos(pos.x.toInt(), y, pos.z.toInt()).add(blockPos),
                            Blocks.OBSIDIAN.defaultState
                        )
                    }
                }, y * ticksBetweenPillarLayer))
            }

            eventScheduler.addEvent(TimedEvent({
                val chestPos = blockPos.up(deathPillarHeight + 1)
                world.setBlockState(chestPos, Blocks.SHULKER_BOX.defaultState, 2)
                LootableInventory.setLootTable(world, actor.random, chestPos, Mod.loot.obsidilith)
            }, deathPillarHeight * ticksBetweenPillarLayer))

            val expTicks = 20
            val expPerTick = (experienceDrop / expTicks.toFloat()).toInt()
            val pillarTop = vecPos.add(VecUtils.yAxis.multiply(deathPillarHeight.toDouble()))
            eventScheduler.addEvent(TimedEvent({
                VanillaCopies.awardExperience(
                    expPerTick,
                    pillarTop.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).multiply(2.0)),
                    world
                )
            }, deathPillarHeight * ticksBetweenPillarLayer, expTicks))
        }
    }
}