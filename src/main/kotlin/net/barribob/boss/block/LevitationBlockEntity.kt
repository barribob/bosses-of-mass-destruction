package net.barribob.boss.block

import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.AnimationUtils
import net.barribob.boss.utils.ModColors
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory

class LevitationBlockEntity(block: Block, type: BlockEntityType<*>?) : ChunkCacheBlockEntity(block, type), IAnimatable,
    Tickable {
    private val particlesFactory = ClientParticleBuilder(Particles.LINE)
        .color(ModColors.COMET_BLUE)
        .brightness(Particles.FULL_BRIGHT)
        .colorVariation(0.25)
        .scale(0.05f)

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(
            AnimationController(
                this,
                "idle",
                0f,
                AnimationUtils.createIdlePredicate("rotate")
            )
        )
    }

    private val animationFactory = AnimationFactory(this)
    override fun getFactory(): AnimationFactory = animationFactory

    override fun tick() {
        super.tick()
        val world = world ?: return
        if (world.isClient) {
            val box = getAffectingBox(world, pos.asVec3d())
            val playersInBox = world.getNonSpectatingEntities(PlayerEntity::class.java, box)
            for (player in playersInBox) {
                for (x in listOf(box.minX, box.maxX)) {
                    val zRand = box.center.z + box.zLength * RandomUtils.double(0.5)
                    particlesFactory.build(randYPos(x, player, zRand))
                }

                for (z in listOf(box.minZ, box.maxZ)) {
                    val xRand = box.center.x + box.xLength * RandomUtils.double(0.5)
                    particlesFactory.build(randYPos(xRand, player, z))
                }
            }
        }
    }

    private fun randYPos(x: Double, player: PlayerEntity, z: Double) = Vec3d(x, player.y + RandomUtils.double(0.5) + 1, z)

    companion object {
        fun tickFlight(player: ServerPlayerEntity) {
            val blockToCheck = mutableListOf<BlockPos>()
            for (x in -1..1) {
                for (z in -1..1) {
                    blockToCheck.add(BlockPos(x * 3, 0, z * 3))
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

            if (!hasLevitationBlock && !player.isCreative) {
                player.abilities.allowFlying = false
                player.abilities.flying = false
                player.networkHandler.sendPacket(PlayerAbilitiesS2CPacket(player.abilities))
            } else if (!player.abilities.allowFlying) {
                player.abilities.allowFlying = true
                player.networkHandler.sendPacket(PlayerAbilitiesS2CPacket(player.abilities))
            }
        }

        private fun getAffectingBox(world: World, pos: Vec3d) =
            Box(pos.x, 0.0, pos.z, (pos.x + 1), world.height.toDouble(), (pos.z + 1)).expand(3.0, 0.0, 3.0)
    }
}