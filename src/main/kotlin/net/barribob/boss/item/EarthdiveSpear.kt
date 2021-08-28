package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.utils.ModUtils.randomPitch
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
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
import net.minecraft.util.UseAction
import net.minecraft.world.World

class EarthdiveSpear(settings: Settings?) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(TranslatableText("item.bosses_of_mass_destruction.earthdive_spear.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if (user is PlayerEntity) {
            val i = getMaxUseTime(stack) - remainingUseTicks
            if (i >= 10) {
                if (!world.isClient && world is ServerWorld) {
                    if (WallTeleport(world, user).tryTeleport(user.rotationVector, user.eyePos) ||
                        WallTeleport(world, user).tryTeleport(user.rotationVector, user.eyePos.add(0.0, -1.0, 0.0))
                    ) {
                        stack.damage(1, user) { it.sendToolBreakStatus(user.getActiveHand()) }
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        world.playSoundFromEntity(
                            null as PlayerEntity?,
                            user,
                            Mod.sounds.earthdiveSpearThrow,
                            SoundCategory.PLAYERS,
                            1.0f,
                            user.random.randomPitch()
                        )
                    }
                }
            }
        }
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        return if (itemStack.damage >= itemStack.maxDamage - 1) {
            TypedActionResult.fail(itemStack)
        } else {
            user.setCurrentHand(hand)
            TypedActionResult.consume(itemStack)
        }
    }

    override fun getMaxUseTime(stack: ItemStack?) = 72000
    override fun getUseAction(stack: ItemStack?) = UseAction.SPEAR
}