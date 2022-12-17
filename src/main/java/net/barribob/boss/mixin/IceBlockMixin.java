package net.barribob.boss.mixin;

import net.barribob.boss.Mod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin {
    @Inject(method = "afterBreak", at = @At("TAIL"))
    private void onAfterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0
                && world instanceof ServerWorld serverWorld
                && serverWorld.getStructureAccessor().getStructureAt(pos, serverWorld.getStructureAccessor().getRegistryManager().get(RegistryKeys.STRUCTURE).get(Mod.INSTANCE.getStructures().getLichStructureRegistry().getConfiguredStructureKey())).hasChildren()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}
