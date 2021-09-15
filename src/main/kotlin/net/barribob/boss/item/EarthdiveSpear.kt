package net.barribob.boss.item

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import net.barribob.boss.Mod
import net.barribob.boss.particle.Particles
import net.barribob.boss.utils.ModUtils.randomPitch
import net.barribob.boss.utils.ModUtils.spawnParticle
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EarthdiveSpear(settings: Settings?) : Item(settings) {
    private val attributeModifiers: Multimap<EntityAttribute?, EntityAttributeModifier?>

    init {
        val builder = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>()
        builder.put(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            EntityAttributeModifier(
                ATTACK_DAMAGE_MODIFIER_ID,
                "Tool modifier",
                8.0,
                EntityAttributeModifier.Operation.ADDITION
            )
        )
        builder.put(
            EntityAttributes.GENERIC_ATTACK_SPEED,
            EntityAttributeModifier(
                ATTACK_SPEED_MODIFIER_ID,
                "Tool modifier",
                -2.9000000953674316,
                EntityAttributeModifier.Operation.ADDITION
            )
        )
        this.attributeModifiers = builder.build()
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(TranslatableText("item.bosses_of_mass_destruction.earthdive_spear.tooltip").formatted(Formatting.DARK_GRAY))
    }

    override fun onStoppedUsing(stack: ItemStack, world: World, user: LivingEntity, remainingUseTicks: Int) {
        if (user is PlayerEntity) {
            if (isCharged(stack, remainingUseTicks)) {
                if (!world.isClient && world is ServerWorld) {
                    if (WallTeleport(world, user).tryTeleport(user.rotationVector, user.eyePos) ||
                        WallTeleport(world, user).tryTeleport(user.rotationVector, user.eyePos.add(0.0, -1.0, 0.0))
                    ) {
                        stack.damage(1, user) { it.sendToolBreakStatus(user.getActiveHand()) }
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        world.playSoundFromEntity(
                            null as PlayerEntity?,
                            user,
                            Mod.sounds.earthdiveSpearThrow,
                            SoundCategory.PLAYERS,
                            1.0f,
                            user.random.randomPitch()
                        )
                    }
                }
            }
        }
    }

    override fun postHit(stack: ItemStack, target: LivingEntity?, attacker: LivingEntity): Boolean {
        stack.damage(1, attacker) {
            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun postMine(
        stack: ItemStack,
        world: World?,
        state: BlockState,
        pos: BlockPos?,
        miner: LivingEntity
    ): Boolean {
        if (state.getHardness(world, pos).toDouble() != 0.0) {
            stack.damage(2, miner) {
                it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
            }
        }
        return true
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack>? {
        val itemStack = user.getStackInHand(hand)
        return if (itemStack.damage >= itemStack.maxDamage - 1) {
            TypedActionResult.fail(itemStack)
        } else {
            user.setCurrentHand(hand)
            TypedActionResult.consume(itemStack)
        }
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        super.usageTick(world, user, stack, remainingUseTicks)
        if (world is ServerWorld) {
            if (isCharged(stack, remainingUseTicks)) {
                val teleportAction: (BlockPos) -> Unit = { spawnTeleportParticles(world, user) }
                val wallTeleport = WallTeleport(world, user)
                if (!wallTeleport.tryTeleport(user.rotationVector, user.eyePos, teleportAction)) {
                    wallTeleport.tryTeleport(user.rotationVector, user.eyePos.add(0.0, -1.0, 0.0), teleportAction)
                }
            }
        }
    }

    private fun spawnTeleportParticles(world: ServerWorld, user: LivingEntity) {
        val pos = user.eyePos.add(user.rotationVector.multiply(0.15)).add(RandomUtils.randVec())
        val vel = user.eyePos.add(user.rotationVector.multiply(4.0)).subtract(pos)
        world.spawnParticle(Particles.EARTHDIVE_INDICATOR, pos, vel, speed = 0.07)
    }

    private fun isCharged(stack: ItemStack, remainingUseTicks: Int): Boolean = getMaxUseTime(stack) - remainingUseTicks >= 10

    override fun getAttributeModifiers(slot: EquipmentSlot): Multimap<EntityAttribute?, EntityAttributeModifier?>? {
        return if (slot == EquipmentSlot.MAINHAND) attributeModifiers else super.getAttributeModifiers(slot)
    }

    override fun getMaxUseTime(stack: ItemStack?) = 72000
    override fun getUseAction(stack: ItemStack?) = UseAction.SPEAR
    override fun getEnchantability() = 1
}