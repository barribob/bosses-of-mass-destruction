package net.barribob.boss.mixin;

import net.barribob.boss.mob.Entities;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
class MobEntityMixin {
    @Inject(method = "isAffectedByDaylight", at = @At("RETURN"), cancellable = true)
    private void handleLichSunShieldingEffect(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        MobEntity mobEntity = (MobEntity) (Object) this;
        boolean lichNearby = !mobEntity.world.getEntitiesByType(Entities.INSTANCE.getLICH(), new Box(mobEntity.getBlockPos()).expand(64), (i) -> true).isEmpty();
        if(lichNearby) {
            cir.setReturnValue(false);
        }
    }
}
