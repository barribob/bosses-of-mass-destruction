package net.barribob.boss.block

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class LevitationBlock(private val factory: (() -> BlockEntity)?, settings: Settings) : Block(settings), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity? = factory?.invoke()

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>,
        options: TooltipContext?
    ) {
        tooltip.add(TranslatableText("item.bosses_of_mass_destruction.levitation_block.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = VoxelShapes.union(bottomShape, tableShape)

    private val bottomShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
    private val tableShape = createCuboidShape(2.0, 2.0, 2.0, 14.0, 14.0, 14.0)
}