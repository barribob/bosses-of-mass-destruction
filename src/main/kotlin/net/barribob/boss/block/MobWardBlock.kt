package net.barribob.boss.block

import com.mojang.serialization.MapCodec
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.text.Text
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MobWardBlock(private val factory: (FabricBlockEntityTypeBuilder.Factory<ChunkCacheBlockEntity>)?, settings: Settings) :
    BlockWithEntity(settings),
    BlockEntityProvider {
    init {
        defaultState = stateManager.defaultState
            .with(HorizontalFacingBlock.FACING, Direction.NORTH)
            .with(tripleBlockPart, TripleBlockPart.BOTTOM)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL
    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): ChunkCacheBlockEntity? = factory?.create(pos, state)
    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return validateTicker(type, ModBlocks.mobWardEntityType, ChunkCacheBlockEntity::tick)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>,
        options: TooltipContext?
    ) {
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.mob_ward.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun getCodec(): MapCodec<out BlockWithEntity> {
        throw UnsupportedOperationException()
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        newState: BlockState,
        world: WorldAccess?,
        pos: BlockPos?,
        posFrom: BlockPos?
    ): BlockState? {
        val thisState = state.get(tripleBlockPart)
        val superState = super.getStateForNeighborUpdate(
            state,
            direction,
            newState,
            world,
            pos,
            posFrom
        )
        val air = Blocks.AIR.defaultState
        val otherState = newState.isOf(this)
        val facingState = if (otherState) state.with(
            HorizontalFacingBlock.FACING,
            newState.get(HorizontalFacingBlock.FACING)
        ) else air

        return when (thisState) {
            TripleBlockPart.BOTTOM -> when (direction) {
                Direction.UP -> if (otherState && newState.get(tripleBlockPart) == TripleBlockPart.MIDDLE) facingState else air
                else -> superState
            }
            TripleBlockPart.MIDDLE -> when (direction) {
                Direction.UP -> if (otherState && newState.get(tripleBlockPart) == TripleBlockPart.TOP) facingState else air
                Direction.DOWN -> if (otherState && newState.get(tripleBlockPart) == TripleBlockPart.BOTTOM) facingState else air
                else -> superState
            }
            TripleBlockPart.TOP -> when (direction) {
                Direction.DOWN -> if (otherState && newState.get(tripleBlockPart) == TripleBlockPart.MIDDLE) facingState else air
                else -> superState
            }
            else -> air
        }
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState? {
        if (!world.isClient && player.isCreative) {
            val part = state.get(tripleBlockPart)
            if (part == TripleBlockPart.MIDDLE) {
                checkBreakPart(pos.down(), world, state, player, TripleBlockPart.BOTTOM)
                checkBreakPart(pos.up(), world, state, player, TripleBlockPart.TOP)
            } else if (part == TripleBlockPart.TOP) {
                checkBreakPart(pos.down(2), world, state, player, TripleBlockPart.BOTTOM)
                checkBreakPart(pos.down(), world, state, player, TripleBlockPart.MIDDLE)
            }
        }

        return super.onBreak(world, pos, state, player)
    }

    private fun checkBreakPart(
        pos: BlockPos,
        world: World,
        state: BlockState,
        player: PlayerEntity,
        part: TripleBlockPart
    ) {
        val blockState2 = world.getBlockState(pos)
        if (blockState2.block === state.block && blockState2.get(tripleBlockPart) == part) {
            world.setBlockState(pos, Blocks.AIR.defaultState, 35)
            world.syncWorldEvent(player, 2001, pos, getRawIdFromState(blockState2))
        }
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val blockPos = ctx.blockPos
        return if (blockPos.y < 254 &&
            ctx.world.getBlockState(blockPos.up()).canReplace(ctx) &&
            ctx.world.getBlockState(blockPos.up(2)).canReplace(ctx)
        ) {
            ctx.world
            defaultState.with(DoorBlock.FACING, ctx.horizontalPlayerFacing).with(tripleBlockPart, TripleBlockPart.BOTTOM)
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
        world.setBlockState(pos.up(), state.with(tripleBlockPart, TripleBlockPart.MIDDLE) as BlockState, 3)
        world.setBlockState(pos.up(2), state.with(tripleBlockPart, TripleBlockPart.TOP) as BlockState, 3)
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val blockPos = pos.down()
        val blockState = world.getBlockState(blockPos)
        return if (state.get(tripleBlockPart) == TripleBlockPart.BOTTOM) blockState.isSideSolidFullSquare(
            world,
            blockPos,
            Direction.UP
        ) else blockState.isOf(this)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(
            HorizontalFacingBlock.FACING, rotation.rotate(
                state.get(HorizontalFacingBlock.FACING)
            )
        )
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return if (mirror == BlockMirror.NONE) state else state.rotate(mirror.getRotation(state.get(DoorBlock.FACING)))
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder!!.add(HorizontalFacingBlock.FACING, tripleBlockPart)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return if (state.get(tripleBlockPart) == TripleBlockPart.TOP) blockShape else thinBlockShape
    }

    private val blueFireParticleFactory = ClientParticleBuilder(Particles.SOUL_FLAME)
        .color(LichUtils.blueColorFade)
        .age(30, 40)
        .colorVariation(0.5)
        .scale { 0.15f - (it * 0.1f) }

    override fun randomDisplayTick(state: BlockState, world: World?, pos: BlockPos, random: Random) {
        if (state.get(tripleBlockPart) == TripleBlockPart.TOP) {
            if (random.nextInt(3) == 0) {
                val vecPos = pos.asVec3d().add(VecUtils.unit.multiply(0.5))
                val randomHeight = RandomUtils.double(0.25) + 0.25
                val randomRadius = RandomUtils.double(0.1) + 0.3
                val randomOffset = RandomUtils.double(Math.PI)
                blueFireParticleFactory.continuousPosition {
                    calcParticlePos(
                        vecPos,
                        randomOffset,
                        randomRadius,
                        randomHeight,
                        it.getAge().toDouble() * 0.1
                    )
                }.build(
                    calcParticlePos(
                        vecPos,
                        randomOffset,
                        randomRadius,
                        randomHeight,
                        .0
                    )
                )
            }
        }
    }

    private fun calcParticlePos(
        vecPos: Vec3d,
        randomOffset: Double,
        randomRadius: Double,
        randomHeight: Double,
        age: Double
    ): Vec3d = vecPos.add(
        Vec3d(
            sin(age + randomOffset) * randomRadius,
            randomHeight + age * 0.3,
            cos(age + randomOffset) * randomRadius
        )
    )

    companion object {
        val blockShape: VoxelShape = createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0)
        val thinBlockShape: VoxelShape = createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
        val tripleBlockPart: EnumProperty<TripleBlockPart> = EnumProperty.of("triple_part", TripleBlockPart::class.java)

        fun canSpawn(serverWorld: ServerWorld, pos: BlockPos.Mutable, cir: CallbackInfoReturnable<Boolean>) {
            if (cir.returnValue == false) return

            val chunkPos = ChunkPos(pos)
            ModComponents.getChunkBlockCache(serverWorld).ifPresent { component ->
                for (x in chunkPos.x - 4..chunkPos.x + 4) {
                    for (z in chunkPos.z - 4..chunkPos.z + 4) {
                        val blocks = component.getBlocksFromChunk(ChunkPos(x, z), ModBlocks.mobWard)
                        if (blocks.any { abs(it.x - pos.x) < 64 && abs(it.y - pos.y) < 64 && abs(it.z - pos.z) < 64 }) {
                            cir.returnValue = false
                        }
                    }
                }
            }
        }
    }
}