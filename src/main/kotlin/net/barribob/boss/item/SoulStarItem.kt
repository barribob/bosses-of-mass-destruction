package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.block.ChiseledStoneAltarBlock
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.mobs.lich.LichUtils
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.particle.ClientParticleBuilder
import net.barribob.boss.particle.ParticleFactories
import net.barribob.boss.particle.Particles
import net.barribob.boss.sound.ModSounds
import net.barribob.boss.utils.ModColors
import net.barribob.boss.utils.ModStructures
import net.barribob.boss.utils.ModUtils
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.EnderEyeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.apache.commons.lang3.RandomUtils
import java.lang.Math.random
import java.util.*

class SoulStarItem(settings: Settings?) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val blockPos = context.blockPos
        val blockState = world.getBlockState(blockPos)
        if (blockState.isOf(ModBlocks.chiseledStoneAltar) && !blockState.get(ChiseledStoneAltarBlock.lit)) {
            return if (world.isClient) {
                clientSoulStartPlace(blockPos)
                ActionResult.SUCCESS
            } else {
                serverSoulStarPlace(blockState, world, blockPos, context)
                world.playSound(
                    null as PlayerEntity?,
                    context.hitPos.x,
                    context.hitPos.y,
                    context.hitPos.z,
                    Mod.sounds.soulStar,
                    SoundCategory.NEUTRAL,
                    0.5f,
                    Random().randomPitch()
                )
                ActionResult.PASS
            }
        }

        return ActionResult.PASS
    }

    private fun serverSoulStarPlace(
        blockState: BlockState,
        world: World,
        blockPos: BlockPos,
        context: ItemUsageContext
    ) {
        val blockState2 = blockState.with(ChiseledStoneAltarBlock.lit, true)
        world.setBlockState(blockPos, blockState2, 2)
        context.stack.decrement(1)
        val altarRange = -60..60
        val numberOfAltarsFilled = altarRange.count {
            val state = world.getBlockState(blockPos.up(it))
            state.contains(ChiseledStoneAltarBlock.lit) && state.get(ChiseledStoneAltarBlock.lit)
        }
        if (numberOfAltarsFilled == 4) {
            altarRange.forEach {
                if (world.getBlockState(blockPos.up(it))
                        .contains(ChiseledStoneAltarBlock.lit)
                ) world.breakBlock(blockPos.up(it), false)
            }

            spawnLich(blockPos, world)
        }
    }

    @Environment(EnvType.CLIENT)
    private fun clientSoulStartPlace(blockPos: BlockPos) {
        val centralPos = blockPos.asVec3d().add(Vec3d(0.5, 1.2, 0.5))
        MathUtils.circleCallback(0.5, 15, VecUtils.yAxis) {
            val particleVel = VecUtils.yAxis.multiply(0.03 + net.barribob.maelstrom.static_utilities.RandomUtils.double(0.01))
            val particlePos = centralPos.add(it)
            ChiseledStoneAltarBlock.blueFireParticleFactory.build(particlePos, particleVel)
        }
    }

    /**
     * [EnderEyeItem.use]
     */
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        val hitResult: HitResult = raycast(world, user, RaycastContext.FluidHandling.NONE)
        return if (hitResult.type == HitResult.Type.BLOCK && world.getBlockState((hitResult as BlockHitResult).blockPos)
                .isOf(ModBlocks.chiseledStoneAltar)
        ) {
            TypedActionResult.pass(itemStack)
        } else {
            user.setCurrentHand(hand)
            if (world is ServerWorld) {
                val blockPos = world.chunkManager.chunkGenerator.locateStructure(
                    world,
                    ModStructures.lichTowerStructure,
                    user.blockPos,
                    500,
                    false
                )
                if (blockPos != null) {
                    val eyeOfEnderEntity = SoulStarEntity(world, user.x, user.eyeY, user.z)
                    eyeOfEnderEntity.setItem(itemStack)
                    eyeOfEnderEntity.initTargetPos(blockPos)
                    world.spawnEntity(eyeOfEnderEntity)
//                    if (user is ServerPlayerEntity) {
//                        Criteria.USED_ENDER_EYE.trigger(user, blockPos)
//                    }
                    world.playSound(
                        null as PlayerEntity?,
                        user.x,
                        user.y,
                        user.z,
                        SoundEvents.ENTITY_ENDER_EYE_LAUNCH,
                        SoundCategory.NEUTRAL,
                        0.5f,
                        0.4f / (RANDOM.nextFloat() * 0.4f + 0.8f)
                    )
                    // plays sound
//                    world.syncWorldEvent(null as PlayerEntity?, 1003, user.blockPos, 0)
                    if (!user.abilities.creativeMode) {
                        itemStack.decrement(1)
                    }
                    user.incrementStat(Stats.USED.getOrCreateStat(this))
                    user.swingHand(hand, true)
                    return TypedActionResult.success(itemStack)
                }
            }
            TypedActionResult.consume(itemStack)
        }
    }

    companion object {
        fun spawnLich(blockPos: BlockPos, world: World) {
            val compoundTag = CompoundTag()
            compoundTag.putString("id", Mod.identifier("lich").toString())

            val spawnPos = blockPos.asVec3d()
            val spawned = MobPlacementLogic(
                HorizontalRangedSpawnPosition(spawnPos, 15.0, 30.0, ModRandom()),
                CompoundTagEntityProvider(compoundTag, world, Mod.LOGGER),
                MobEntitySpawnPredicate(world),
                SimpleMobSpawner(world as ServerWorld)
            ).tryPlacement(200)

            if (!spawned) {
                val entity = Entities.LICH.create(world)
                if (entity != null) {
                    val defaultSpawnPos = spawnPos.add(VecUtils.xAxis.multiply(5.0))
                    entity.updateTrackedPosition(defaultSpawnPos)
                    entity.updatePosition(defaultSpawnPos.x, defaultSpawnPos.y, defaultSpawnPos.z)
                    world.spawnEntity(entity)
                }
            }
        }
    }
}