package net.barribob.boss.item

import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class CrystalFruitItem(settings: Settings?) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext?,
        tooltip: MutableList<Text>,
        type: TooltipType?
    ) {
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.crystal_fruit.tooltip").formatted(Formatting.DARK_GRAY))
    }
}