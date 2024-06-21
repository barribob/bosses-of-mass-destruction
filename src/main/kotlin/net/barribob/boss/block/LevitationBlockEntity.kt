package net.barribob.boss.block

import me.shedaniel.autoconfig.AutoConfig
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.config.ModConfig
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib.animatable.GeoBlockEntity
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.animation.AnimatableManager
import software.bernie.geckolib.animation.AnimationController
import software.bernie.geckolib.animation.RawAnimation
import software.bernie.geckolib.util.GeckoLibUtil


class LevitationBlockEntity(
    block: Block, type: BlockEntityType<*>?,
    pos: BlockPos?, state: BlockState?
) : ChunkCacheBlockEntity(block, type, pos, state), GeoBlockEntity {
    override fun registerControllers(data: AnimatableManager.ControllerRegistrar) {
        data.add(
            AnimationController(this) { it.setAndContinue(rotate) }
        )
    }

    var animationAge = 0
    private val animationFactory = GeckoLibUtil.createInstanceCache(this)
    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = animationFactory

    companion object {
        val rotate: RawAnimation = RawAnimation.begin().thenLoop("rotate")
        private val flight = HashSet<ServerPlayerEntity>()
        private val particlesFactory = ClientParticleBuilder(Particles.LINE)
            .color(ModColors.COMET_BLUE)
            .brightness(Particles.FULL_BRIGHT)
            .colorVariation(0.5)
            .scale(0.075f)
        private val tableOfElevationRadius = AutoConfig.getConfigHolder(ModConfig::class.java).config.generalConfig.tableOfElevationRadius.toDouble()
        
        fun tick(world: World, pos: BlockPos, state: BlockState, entity: LevitationBlockEntity) {
            ChunkCacheBlockEntity.tick(world, pos, state, entity)
            if (world.isClient) {
                entity.animationAge++
                val box = getAffectingBox(world, pos.asVec3d())
                val playersInBox = world.getNonSpectatingEntities(PlayerEntity::class.java, box)
                for (player in playersInBox) {
                    for (x in listOf(box.minX, box.maxX)) {
                        val zRand = box.center.z + box.lengthZ * RandomUtils.double(0.5)
                        particlesFactory.build(randYPos(x, player, zRand))
                    }

                    for (z in listOf(box.minZ, box.maxZ)) {
                        val xRand = box.center.x + box.lengthX * RandomUtils.double(0.5)
                        particlesFactory.build(randYPos(xRand, player, z))
                    }
                }
            }
        }

        private fun randYPos(x: Double, player: PlayerEntity, z: Double) =
            Vec3d(x, player.y + RandomUtils.double(0.5) + 1, z)

        fun tickFlight(player: ServerPlayerEntity) {
            val blockToCheck = mutableListOf<BlockPos>()
            for (x in -1..1) {
                for (z in -1..1) {
                    blockToCheck.add(BlockPos(x * tableOfElevationRadius.toInt(), 0, z * tableOfElevationRadius.toInt()))
                }
            }
            val chunksToCheck = blockToCheck.map { ChunkPos(it.add(player.blockPos)) }.toSet()
            val hasLevitationBlock = chunksToCheck.any { it ->
                val blockCache = ModComponents.getChunkBlockCache(player.world)

                if (blockCache.isPresent) {
                    val blocks = blockCache.get().getBlocksFromChunk(it, ModBlocks.levitationBlock)
                    blocks.any { getAffectingBox(player.world, it.asVec3d()).contains(player.pos) }
                } else {
                    false
                }
            }

            /**
             * Known bugs:
             * - does not have persistence (e.g. loading why flying will fall next time it is opened, though without fall damage)
             * - does not handle changing gamemodes that change these abilities (e.g. changing to survival or losing the flying ability
             * from taking another modded item off will not work as expected.)
             * - However, it will be more-or-less able to work with other mods that mess with the flight ability provided that
             * they do not set it every tick
             */
            if (!hasLevitationBlock) {
                if (flight.contains(player)) {
                    if (!player.isCreative && !player.isSpectator) {
                        player.abilities.allowFlying = false
                        player.abilities.flying = false
                        player.networkHandler.sendPacket(PlayerAbilitiesS2CPacket(player.abilities))
                    }
                    flight.remove(player)
                }
            } else if (!flight.contains(player)) {
                flight.add(player)
                if (!player.abilities.allowFlying) {
                    player.abilities.allowFlying = true
                    player.networkHandler.sendPacket(PlayerAbilitiesS2CPacket(player.abilities))
                }
            }
        }

        private fun getAffectingBox(world: World, pos: Vec3d) : Box {
            return Box(pos.x, world.bottomY.toDouble(), pos.z, (pos.x + 1), world.height.toDouble(), (pos.z + 1))
                .expand(tableOfElevationRadius, 0.0, tableOfElevationRadius)
        }
    }
}