package net.barribob.boss.mixin;

import net.barribob.boss.block.ObsidilithSummonBlock;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeUseMixin {
    @Inject(method="useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onEyeUsed(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir){
        ObsidilithSummonBlock.Companion.onEnderEyeUsed(context, cir);
    }
}
