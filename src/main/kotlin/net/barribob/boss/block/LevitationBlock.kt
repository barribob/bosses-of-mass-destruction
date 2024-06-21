package net.barribob.boss.block

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class LevitationBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<LevitationBlockEntity>)?, settings: Settings) : BlockWithEntity(settings), BlockEntityProvider {
    override fun appendTooltip(
        stack: ItemStack?, 
        context: Item.TooltipContext?, 
        tooltip: MutableList<Text>?, 
        options: TooltipType?
    ) {
        tooltip?.add(Text.translatable("item.bosses_of_mass_destruction.levitation_block.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = VoxelShapes.union(bottomShape, tableShape)

    private val bottomShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
    private val tableShape = createCuboidShape(2.0, 2.0, 2.0, 14.0, 14.0, 14.0)
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? = factory?.create(pos, state)
    override fun getCodec(): MapCodec<out BlockWithEntity> {
        throw UnsupportedOperationException()
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return validateTicker(type, ModBlocks.levitationBlockEntityType, LevitationBlockEntity::tick)
    }
}