package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.utils.VanillaCopies
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import kotlin.math.abs

class MonolithBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<ChunkCacheBlockEntity>)?, settings: Settings) : BlockWithEntity(settings),
    BlockEntityProvider {

    init {
        defaultState = stateManager.defaultState
            .with(HorizontalFacingBlock.FACING, Direction.NORTH)
            .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>,
        options: TooltipContext?
    ) {
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.monolith_block.tooltip_0").formatted(Formatting.DARK_GRAY))
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.monolith_block.tooltip_1").formatted(Formatting.DARK_GRAY))
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? = factory?.create(pos, state)
    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return checkType(type, ModBlocks.monolithEntityType, ChunkCacheBlockEntity::tick)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return if (state.get(HorizontalFacingBlock.FACING).axis === Direction.Axis.X) xAxisShape else zAxisShape
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        newState: BlockState,
        world: WorldAccess?,
        pos: BlockPos?,
        posFrom: BlockPos?
    ): BlockState? {
        val doubleBlockHalf = state.get(Properties.DOUBLE_BLOCK_HALF)
        val airState = Blocks.AIR.defaultState
        return if (direction.axis === Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (newState.isOf(this) && newState.get(Properties.DOUBLE_BLOCK_HALF) != doubleBlockHalf) {
                state.with(HorizontalFacingBlock.FACING, newState.get(HorizontalFacingBlock.FACING))
            } else {
                airState
            }
        } else {
            super.getStateForNeighborUpdate(
                state,
                direction,
                newState,
                world,
                pos,
                posFrom
            )
        }
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        if (!world.isClient && player.isCreative) {
            VanillaCopies.onBreakInCreative(world, pos, state, player)
        }
        super.onBreak(world, pos, state, player)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val blockPos = ctx.blockPos
        return if (blockPos.y < 255 && ctx.world.getBlockState(blockPos.up()).canReplace(ctx)) {
            defaultState.with(HorizontalFacingBlock.FACING, ctx.horizontalPlayerFacing)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
        } else {
            null
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        world.setBlockState(pos.up(), state.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER) as BlockState, 3)
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.down()
        val blockState = world.getBlockState(blockPos)
        return if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) blockState.isSideSolidFullSquare(
            world,
            blockPos,
            Direction.UP
        ) else blockState.isOf(this)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(
            HorizontalFacingBlock.FACING,
            rotation.rotate(state.get(HorizontalFacingBlock.FACING) as Direction)
        )
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return if (mirror == BlockMirror.NONE) state else state.rotate(
            mirror.getRotation(
                state.get(
                    HorizontalFacingBlock.FACING
                )
            )
        ).cycle(DoorBlock.HINGE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(Properties.DOUBLE_BLOCK_HALF, HorizontalFacingBlock.FACING)
    }

    companion object {
        private val xAxisShape = createCuboidShape(3.5, 0.0, 1.5, 12.5, 16.0, 14.5)
        private val zAxisShape = createCuboidShape(1.5, 0.0, 3.5, 14.5, 16.0, 12.5)

        fun getExplosionPower(
            serverWorld: World,
            pos: BlockPos,
            power: Float
        ): Float {
            val chunkPos = ChunkPos(pos)
            val blockCache = ModComponents.getChunkBlockCache(serverWorld)
            if (blockCache.isPresent) {
                for (x in chunkPos.x - 4..chunkPos.x + 4) {
                    for (z in chunkPos.z - 4..chunkPos.z + 4) {
                        val blocks = blockCache.get().getBlocksFromChunk(ChunkPos(x, z), ModBlocks.monolithBlock)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.y - pos.y) < 64 && abs(it.z - pos.z) < 64 }) {
                            return power * 1.3f
                        }
                    }
                }
            }

            return power
        }
    }
}