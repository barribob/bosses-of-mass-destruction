package net.barribob.boss.mixin;

import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureRegisterInvoker {
    @Invoker("register")
    static <F extends StructureFeature<?>> F invokeRegister(String name, F structureFeature, GenerationStep.Feature step) {
       throw new AssertionError();
    }
}
