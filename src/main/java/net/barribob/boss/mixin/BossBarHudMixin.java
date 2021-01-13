package net.barribob.boss.mixin;

import net.barribob.boss.mob.mobs.lich.LichUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private static Identifier BARS_TEXTURE;

    @Inject(
            method = "renderBossBar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomBossBar(MatrixStack matrices, int x, int y, BossBar bossBar, CallbackInfo ci) {
        LichUtils.INSTANCE.renderBossBar(matrices, x, y, bossBar, ci);
        this.client.getTextureManager().bindTexture(BARS_TEXTURE);
    }
}
