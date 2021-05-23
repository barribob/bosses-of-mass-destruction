package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.utils.VanillaCopies
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.explosion.Explosion
import java.util.*
import kotlin.math.abs

class MonolithBlock(private val factory: (() -> BlockEntity)?, settings: Settings) : Block(settings),
    BlockEntityProvider {
    init {
        defaultState = stateManager.defaultState
            .with(HorizontalFacingBlock.FACING, Direction.NORTH)
            .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? = factory?.invoke()

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
            if (newState.isOf(this) && newState.get(Properties.DOUBLE_BLOCK_HALF) != doubleBlockHalf)
                state.with(HorizontalFacingBlock.FACING, newState.get(HorizontalFacingBlock.FACING)) else airState
        } else {
            if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(
                    world,
                    pos
                )
            )
                airState else
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
            defaultState.with(HorizontalFacingBlock.FACING, ctx.playerFacing)
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
        ) as BlockState
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return if (mirror == BlockMirror.NONE) state else state.rotate(
            mirror.getRotation(
                state.get(
                    HorizontalFacingBlock.FACING
                ) as Direction
            )
        ).cycle(DoorBlock.HINGE) as BlockState
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(Properties.DOUBLE_BLOCK_HALF, HorizontalFacingBlock.FACING)
    }

    companion object {
        private val hashMap = WeakHashMap<BlockPos, Boolean>()

        fun canExplode(
            serverWorld: World,
            pos: BlockPos
        ): Boolean {
            val chunkPos = ChunkPos(pos)
            for (x in chunkPos.x - 4..chunkPos.x + 4) {
                for (z in chunkPos.z - 4..chunkPos.z + 4) {
                    val chunk = serverWorld.getChunk(x, z, ChunkStatus.FULL)
                    val blockCache = ModComponents.getChunkBlockCache(chunk)

                    if (blockCache.isPresent) {
                        val blocks = blockCache.get().getBlocksFromChunk(ModBlocks.monolithBlock)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.y - pos.y) < 64 && abs(it.z - pos.z) < 64 }) {
                            return false
                        }
                    }
                }
            }

            return true
        }
    }
}