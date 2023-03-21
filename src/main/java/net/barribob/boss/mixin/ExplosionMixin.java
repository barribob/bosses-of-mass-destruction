package net.barribob.boss.mixin;

import net.barribob.boss.block.MonolithBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerWorld.class)
public abstract class ExplosionMixin {
    @ModifyVariable(at = @At(value = "HEAD"), method = "createExplosion", argsOnly = true)
    private float Explosion(float g, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior explosionBehavior, double d, double e, double f) {
        World world = (World) (Object) this;
        if (!world.isClient) {
            return MonolithBlock.Companion.getExplosionPower(world, BlockPos.ofFloored(d, e, f), g);
        }

        return g;
    }
}
