package net.barribob.boss.block

import net.barribob.boss.Mod
import net.barribob.boss.block.structure_repair.StructureRepair
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.Vec3dNetworkHandler.Companion.sendVec3dPacket
import net.barribob.boss.utils.VecId
import net.barribob.maelstrom.general.event.TimedEvent
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BrimstoneNectarItem(settings: Settings, private val structureRepairs: List<StructureRepair>) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(TranslatableText("item.bosses_of_mass_destruction.brimstone_nectar.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        if (!world.isClient && world is ServerWorld) {
            val usePos = user.blockPos
            val structuresToRepair = findStructuresToRepair(world, usePos)

            if(structuresToRepair.isNotEmpty()) {
                scheduleStructureRepair(world, structuresToRepair, usePos)
                playSound(world, user)
                user.itemCooldownManager[this] = 80
                world.sendVec3dPacket(user.pos, VecId.BrimstoneParticleEffect)

                if (!user.abilities.creativeMode) {
                    itemStack.decrement(1)
                }

                user.incrementStat(Stats.USED.getOrCreateStat(this))
                return TypedActionResult.success(itemStack, world.isClient())
            }
        }

        return TypedActionResult.pass(itemStack)
    }

    private fun findStructuresToRepair(
        world: ServerWorld,
        usePos: BlockPos
    ) = structureRepairs
        .filter {
            val structureStart = getStructureStart(world, usePos, it)
            structureStart.hasChildren() && it.shouldRepairStructure(world, structureStart)
        }

    private fun scheduleStructureRepair(
        world: ServerWorld,
        structuresToRepair: List<StructureRepair>,
        usePos: BlockPos
    ) {
        ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
            structuresToRepair.forEach { it.repairStructure(world, getStructureStart(world, usePos, it)) }
        }, 30))
    }

    private fun getStructureStart(
        world: ServerWorld,
        blockPos: BlockPos,
        it: StructureRepair
    ) = world.structureAccessor.getStructureAt(blockPos, it.associatedStructure())

    private fun playSound(world: World, user: PlayerEntity) {
        world.playSound(
            null as PlayerEntity?,
            user.x,
            user.y,
            user.z,
            Mod.sounds.brimstone,
            SoundCategory.NEUTRAL,
            1.0f,
            world.getRandom().randomPitch()
        )
    }
}