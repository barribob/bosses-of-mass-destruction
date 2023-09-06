package net.barribob.boss.block

import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

open class ChiseledStoneAltarBlock(settings: Settings) : Block(settings) {
    init {
        defaultState = stateManager.defaultState.with(lit, false)
    }

    override fun getPlacementState(ctx: ItemPlacementContext) = defaultState.with(lit, false) as BlockState

    override fun hasComparatorOutput(state: BlockState?) = true
    override fun getComparatorOutput(state: BlockState, world: World?, pos: BlockPos?): Int {
        return if (state.get(lit) as Boolean) 15 else 0
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        builder.add(lit)
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (state.get(lit)) {
            if(random.nextInt(3) == 0) {
                blueFireParticleFactory.build(
                    pos.asVec3d().add(0.5, 1.0, 0.5)
                        .add(RandomUtils.randVec().planeProject(VecUtils.yAxis).multiply(0.5)),
                    VecUtils.yAxis.multiply(0.05)
                )
            }
        } else {
            paleSparkleParticleFactory.build(
                pos.asVec3d().add(0.5, 2.0, 0.5).add(RandomUtils.randVec().multiply(0.5)),
                VecUtils.yAxis.multiply(-0.05)
            )
        }
    }

    companion object {
        val lit: BooleanProperty = Properties.LIT

        private val paleSparkleParticleFactory = ClientParticleBuilder(Particles.DOWNSPARKLE)
            .color { MathUtils.lerpVec(it, ModColors.WHITE, ModColors.GREY) }
            .age(20, 30)
            .colorVariation(0.1)
            .scale { 0.15f - (it * 0.1f) }
        val blueFireParticleFactory = ParticleFactories.soulFlame()
            .color(LichUtils.blueColorFade)
            .age(30, 40)
            .colorVariation(0.5)
            .scale { 0.15f - (it * 0.1f) }

    }
}