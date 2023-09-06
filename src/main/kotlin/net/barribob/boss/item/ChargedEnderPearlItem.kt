package net.barribob.boss.item

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class ChargedEnderPearlItem(settings: Settings?) : Item(settings) {
    val itemCooldown = 180

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        world.playSound(
            null as PlayerEntity?,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENTITY_ENDER_PEARL_THROW,
            SoundCategory.NEUTRAL,
            0.5f,
            0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f)
        )

        user.itemCooldownManager[this] = itemCooldown

        if (!world.isClient) {
            val projectile = ChargedEnderPearlEntity(world, user)
            projectile.setItem(itemStack)
            projectile.setVelocity(user, user.pitch, user.yaw, 0.0f, 1.5f, 1.0f)
            world.spawnEntity(projectile)
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this))
        return TypedActionResult.success(itemStack, world.isClient())
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.charged_ender_pearl.tooltip").formatted(Formatting.DARK_GRAY))
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.charged_ender_pearl.tooltip2").formatted(Formatting.DARK_GRAY))
    }
}