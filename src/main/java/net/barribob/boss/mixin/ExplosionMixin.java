package net.barribob.boss.mixin;

import net.barribob.boss.block.MonolithBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private double x;

    @Shadow
    @Final
    private double y;

    @Shadow
    @Final
    private double z;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionBehavior;canDestroyBlock(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)Z"), method = "collectBlocksAndDamageEntities")
    private boolean Explosion(ExplosionBehavior explosionBehavior, Explosion explosion, BlockView blockView, BlockPos pos, BlockState state, float power) {
        boolean canDestroyBlock = explosionBehavior.canDestroyBlock(explosion, blockView, pos, state, power);
        if (canDestroyBlock && world instanceof ServerWorld) {
            return MonolithBlock.Companion.canExplode(world, new BlockPos(x, y, z));
        }

        return canDestroyBlock;
    }
}
