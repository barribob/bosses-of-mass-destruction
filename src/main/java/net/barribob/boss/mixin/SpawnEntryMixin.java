package net.barribob.boss.mixin;

import net.barribob.boss.utils.ModStructures;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpawnHelper.class)
public abstract class SpawnEntryMixin {
    @Inject(method = "method_29950", at = @At("HEAD"), cancellable = true)
    private static void onGetSpawnEntries(ServerWorld serverWorld, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos blockPos, Biome biome, CallbackInfoReturnable<List<SpawnSettings.SpawnEntry>> cir) {
        ModStructures.INSTANCE.onGetSpawnEntries(structureAccessor, spawnGroup, blockPos, cir);
    }
}
