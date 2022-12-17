package net.barribob.boss.item

import net.barribob.boss.Mod
import net.barribob.boss.block.ChiseledStoneAltarBlock
import net.barribob.boss.block.ModBlocks
import net.barribob.boss.cardinalComponents.ModComponents
import net.barribob.boss.mob.Entities
import net.barribob.boss.mob.spawn.*
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.maelstrom.general.event.TimedEvent
import net.barribob.maelstrom.general.random.ModRandom
import net.barribob.maelstrom.static_utilities.MathUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.barribob.maelstrom.static_utilities.asVec3d
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.EnderEyeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

class SoulStarItem(settings: Settings?) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(Text.translatable("item.bosses_of_mass_destruction.soul_star.tooltip").formatted(Formatting.DARK_GRAY))
    }

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
                    world.random.randomPitch()
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
        val quarterAltarPosition = listOf(BlockPos(12, 0, 0), BlockPos(6, 0, 6))
        val allPotentialAltarPositions = BlockRotation.values().flatMap { rot -> quarterAltarPosition.map { it.rotate(rot) } }
        val numberOfAltarsFilled = allPotentialAltarPositions.count {
            val state = world.getBlockState(blockPos.add(it))
            state.contains(ChiseledStoneAltarBlock.lit) && state.get(ChiseledStoneAltarBlock.lit)
        }
        if (numberOfAltarsFilled == 3) {
            val eventScheduler = ModComponents.getWorldEventScheduler(world)
            eventScheduler.addEvent(TimedEvent({
                allPotentialAltarPositions.forEach {
                    if (world.getBlockState(blockPos.add(it))
                            .contains(ChiseledStoneAltarBlock.lit)
                    ) world.breakBlock(blockPos.add(it), false)
                }

                world.breakBlock(blockPos, false)

                spawnLich(blockPos, world)
            }, 20))
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
                val blockPos = world.locateStructure(Mod.structures.soulStarStructureKey, user.blockPos,  100, false)
                if (blockPos != null) {
                    val entity = SoulStarEntity(world, user.x, user.eyeY, user.z)
                    entity.setItem(itemStack)
                    entity.initTargetPos(blockPos)
                    world.spawnEntity(entity)
                    world.playSound(
                        null as PlayerEntity?,
                        user.x,
                        user.y,
                        user.z,
                        SoundEvents.ENTITY_ENDER_EYE_LAUNCH,
                        SoundCategory.NEUTRAL,
                        0.5f,
                        0.4f / (world.random.nextFloat() * 0.4f + 0.8f)
                    )
                    if (!user.abilities.creativeMode) {
                        itemStack.decrement(1)
                    }
                    user.incrementStat(Stats.USED.getOrCreateStat(this))
                    user.swingHand(hand, true)
                    return TypedActionResult.success(itemStack)
                }
                return TypedActionResult.pass(itemStack)
            }
            TypedActionResult.consume(itemStack)
        }
    }

    companion object {
        fun spawnLich(blockPos: BlockPos, world: World) {
            val compoundTag = NbtCompound()
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
                    entity.updateTrackedPosition(defaultSpawnPos.x, defaultSpawnPos.y, defaultSpawnPos.z)
                    entity.updatePosition(defaultSpawnPos.x, defaultSpawnPos.y, defaultSpawnPos.z)
                    world.spawnEntity(entity)
                }
            }
        }
    }
}