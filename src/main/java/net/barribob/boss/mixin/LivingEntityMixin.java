package net.barribob.boss.mixin;

import net.barribob.boss.damageSource.UnshieldableDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "blockedByShield", at = @At("HEAD"), cancellable = true)
    private void handleCustomUnshieldableDamageSource(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if(source instanceof UnshieldableDamageSource) {
            cir.setReturnValue(false);
        }
    }
}
