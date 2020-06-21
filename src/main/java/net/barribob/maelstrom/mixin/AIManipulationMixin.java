package net.barribob.maelstrom.mixin;

import kotlin.Pair;
import net.barribob.maelstrom.MaelstromMod;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class AIManipulationMixin {

    @Shadow @Final protected GoalSelector goalSelector;

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void goal(CallbackInfo info) {
        MobEntity entity = (MobEntity) (Object) this;
        if (entity.world != null && !entity.world.isClient) {
            if (MaelstromMod.INSTANCE.getAiManager().getInjections().containsKey(entity.getType())) {
                MaelstromMod.INSTANCE.getAiManager().getInjections().get(entity.getType()).forEach((generator) -> {
                    Pair<Integer, Goal> pair = generator.invoke(entity);
                    this.goalSelector.add(pair.component1(), pair.component2());
                });
            }
        }
    }
}
