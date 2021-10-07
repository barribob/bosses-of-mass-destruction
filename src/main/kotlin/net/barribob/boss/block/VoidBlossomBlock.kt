package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity
import net.barribob.boss.mob.utils.EntityAdapter
import net.barribob.boss.mob.utils.EntityStats
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.NetworkUtils.Companion.sendHealPacket
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.static_utilities.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import java.util.*

class VoidBlossomBlock(settings: Settings?) : Block(settings) {
    private val shape = createCuboidShape(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
    private val spikeParticleFactory = ClientParticleBuilder(Particles.LINE)
        .color { MathUtils.lerpVec(it, ModColors.VOID_PURPLE, ModColors.ULTRA_DARK_PURPLE) }
        .colorVariation(0.15)
        .brightness(Particles.FULL_BRIGHT)
        .scale(0.25f)
        .age(10, 15)

    private fun healNearbyEntities(world: ServerWorld, pos: BlockPos) {
        world.getEntitiesByType(Entities.VOID_BLOSSOM, Box(pos).expand(40.0, 20.0, 40.0)) { true }.forEach {
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                LichUtils.cappedHeal(EntityAdapter(it), EntityStats(it), VoidBlossomEntity.hpMilestones, 10f, it::heal)
            }, healAnimationDelay))
            it.sendHealPacket(pos.asVec3d().add(VecUtils.unit.multiply(0.5)), it.pos.add(VecUtils.yAxis.multiply(0.5)))
        }
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        world.blockTickScheduler.schedule(pos, this, healDelay)
        healNearbyEntities(world, pos)
    }

    override fun onBlockAdded(
        state: BlockState,
        world: World,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean
    ) {
        world.blockTickScheduler.schedule(pos, this, 1)
    }

    override fun canPlaceAt(state: BlockState?, world: WorldView, pos: BlockPos): Boolean {
        return sideCoversSmallSquare(world, pos.down(), Direction.UP) && !world.isWater(pos)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState?,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos?
    ): BlockState? {
        return if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
            onBroken(world, pos, state)
            Blocks.AIR.defaultState
        } else {
            super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
        }
    }

    override fun onBroken(world: WorldAccess, pos: BlockPos, state: BlockState) {
        for(x in -1..1) {
            for (z in -1..1) {
                for(y in -1..1) {
                    if((x != 0 || z != 0)) {
                        val pos1 = pos.add(x, y, z)
                        if(world.getBlockState(pos1).block == ModBlocks.vineWall) {
                            world.blockTickScheduler.schedule(pos1, ModBlocks.vineWall, (2 - y) * 20 + RandomUtils.range(0, 19))
                        }
                    }
                }
            }
        }
    }

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = shape

    override fun onBreak(world: World, pos: BlockPos, state: BlockState?, player: PlayerEntity) {
        if (world.isClient) {
            for (i in 0 until 12) {
                val vel = VecUtils.yAxis.multiply(RandomUtils.range(0.1, 0.2))
                val spawnPos =
                    pos.asVec3d().add(VecUtils.unit.multiply(0.5)).add(RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().multiply(0.5))
                spikeParticleFactory.build(spawnPos, vel)
            }
        }
        super.onBreak(world, pos, state, player)
    }

    companion object {
        private const val healAnimationDelay = 16
        private const val healDelay = 64
        private val healParticleFactory = ClientParticleBuilder(Particles.OBSIDILITH_BURST)
            .color { MathUtils.lerpVec(it, ModColors.PINK, ModColors.ULTRA_DARK_PURPLE) }
            .colorVariation(0.15)
            .brightness(Particles.FULL_BRIGHT)
            .scale { 0.4f * (1 - it * 0.75f) }
            .age(10)

        private val petalParticleFactory = ClientParticleBuilder(Particles.PETAL)
            .color { MathUtils.lerpVec(it, ModColors.PINK, ModColors.ULTRA_DARK_PURPLE) }
            .brightness(Particles.FULL_BRIGHT)
            .colorVariation(0.15)
            .scale { 0.15f * (1 - it * 0.25f) }
            .age(30)

        @Environment(EnvType.CLIENT)
        fun handleVoidBlossomHeal(world: ClientWorld, source: Vec3d, dest: Vec3d) {
            spawnHealParticle(dest, source, world)
            spawnChargeParticle(source, world)
        }

        @Environment(EnvType.CLIENT)
        private fun spawnChargeParticle(source: Vec3d, world: ClientWorld) {
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                healParticleFactory.build(source.add(RandomUtils.randVec().multiply(0.1)))
            }, 32, 32))
        }

        @Environment(EnvType.CLIENT)
        private fun spawnHealParticle(
            dest: Vec3d,
            source: Vec3d,
            world: ClientWorld
        ) {
            val particlePositions = mutableListOf<Vec3d>()
            val numCirclePoints = healAnimationDelay
            val circlePoints = MathUtils.circlePoints(0.5, numCirclePoints, dest.subtract(source).normalize()).toList()
            MathUtils.lineCallback(source, dest, 32) { pos, i ->
                particlePositions.add(pos.add(circlePoints[i % numCirclePoints]))
            }

            var i = 0
            ModComponents.getWorldEventScheduler(world).addEvent(TimedEvent({
                healParticleFactory.build(particlePositions[i])
                healParticleFactory.build(particlePositions[i + 1])
                i += 2
            }, 0, healAnimationDelay))
        }

        fun handleVoidBlossomPlace(pos: Vec3d) {
            for (i in 0..12) {
                val spawnPos = pos.add(RandomUtils.randVec().planeProject(VecUtils.yAxis).normalize().multiply(0.5))
                val vel = VecUtils.yAxis.multiply(RandomUtils.range(0.1, 0.3))
                val randomRot = RandomUtils.range(0, 360)
                val angularMomentum = RandomUtils.randSign() * 4f

                petalParticleFactory
                    .continuousRotation { randomRot + it.getAge() * angularMomentum }
                    .continuousVelocity { vel.multiply(1.0 - it.ageRatio) }
                    .build(spawnPos, vel)
            }
        }
    }
}