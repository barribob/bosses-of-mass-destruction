package net.barribob.boss.mixin;

import net.minecraft.datafixer.fix.StructuresToConfiguredStructuresFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StructuresToConfiguredStructuresFix.class)
public interface DataFixMixin {
    @Mutable
    @Accessor("STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING")
    static Map<String, StructuresToConfiguredStructuresFix.Mapping> bossesOfMassDestruction_getStructures(){
        throw new AssertionError();
    }

    @Mutable
    @Accessor("STRUCTURE_TO_CONFIGURED_STRUCTURES_MAPPING")
    static void bossesOfMassDestruction_setStructures(Map<String, StructuresToConfiguredStructuresFix.Mapping> mapping) {
        throw new AssertionError();
    }
}
