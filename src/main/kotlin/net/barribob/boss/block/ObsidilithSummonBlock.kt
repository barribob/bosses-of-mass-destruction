package net.barribob.boss.block

import net.barribob.boss.block.ModBlocks.obsidilithSummonBlock
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.maelstrom.general.event.EventScheduler
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.sin

open class ObsidilithSummonBlock(settings: Settings) : Block(settings) {
    init {
        defaultState = stateManager.defaultState.with(eye, false)
    }

    override fun hasSidedTransparency(state: BlockState?): Boolean {
        return true
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return if (state.get(eye)) frameWithEyeShape else frameShape
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(eye, false) as BlockState
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean {
        return true
    }

    override fun getComparatorOutput(state: BlockState, world: World?, pos: BlockPos?): Int {
        return if (state.get(eye) as Boolean) 15 else 0
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(eye)
    }

    companion object {
        val eye: BooleanProperty = Properties.EYE
        protected val frameShape: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 13.0, 16.0)
        protected val eyeShape: VoxelShape = createCuboidShape(4.0, 13.0, 4.0, 12.0, 16.0, 12.0)
        protected val frameWithEyeShape: VoxelShape = VoxelShapes.union(frameShape, eyeShape)

        fun onEnderEyeUsed(context: ItemUsageContext, cir: CallbackInfoReturnable<ActionResult>) {
            val world = context.world
            val blockPos = context.blockPos
            val blockState = world.getBlockState(blockPos)
            if (blockState.isOf(obsidilithSummonBlock) && !blockState.get(EndPortalFrameBlock.EYE)) {
                val eventScheduler = ModComponents.getWorldEventScheduler(world)
                if (world.isClient) {
                    cir.returnValue = ActionResult.SUCCESS
                    addSummonEntityEffects(eventScheduler, blockPos)
                } else {
                    val blockState2 = blockState.with(EndPortalFrameBlock.EYE, true)
                    pushEntitiesUpBeforeBlockChange(blockState, blockState2, world, blockPos)
                    world.setBlockState(blockPos, blockState2, 2)
                    context.stack.decrement(1)
                    world.syncWorldEvent(1503, blockPos, 0)

                    addSummonEntityEvent(eventScheduler, world, blockPos)
                    cir.returnValue = ActionResult.PASS
                }
            }
        }

        private val activateParticleFactory = ClientParticleBuilder(Particles.PILLAR_RUNE)
            .scale { (sin(it.toDouble() * Math.PI)).toFloat() * 0.05f }
            .age(30)

        @Environment(EnvType.CLIENT)
        private fun addSummonEntityEffects(
            eventScheduler: EventScheduler,
            blockPos: BlockPos
        ) {
            val centralPos = blockPos.up().asVec3d().add(VecUtils.unit.multiply(0.5))
            val particleVel = VecUtils.yAxis.multiply(-0.03)
            eventScheduler.addEvent(TimedEvent({
                activateParticleFactory.build(
                    centralPos.add(RandomUtils.randVec().multiply(2.0)),
                    particleVel
                )
            }, 0, 80))
        }

        private fun addSummonEntityEvent(
            eventScheduler: EventScheduler,
            world: World,
            blockPos: BlockPos
        ) {
            val pos = blockPos.asVec3d().add(Vec3d(0.5, 0.0, 0.5))
            eventScheduler.addEvent(TimedEvent({
                world.setBlockState(blockPos, Blocks.AIR.defaultState)
                val obsidilithEntity = Entities.OBSIDILITH.create(world)
                if (obsidilithEntity != null) {
                    obsidilithEntity.updateTrackedPosition(pos)
                    obsidilithEntity.updatePosition(pos.x, pos.y, pos.z)
                    world.spawnEntity(obsidilithEntity)
                }
            }, 100))
        }
    }
}