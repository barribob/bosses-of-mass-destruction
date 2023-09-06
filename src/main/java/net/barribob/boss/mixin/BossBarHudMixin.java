package net.barribob.boss.mixin;

import net.barribob.boss.Mod;
import net.barribob.boss.mob.Entities;
import net.barribob.boss.mob.mobs.lich.LichUtils;
import net.barribob.boss.mob.mobs.obsidilith.ObsidilithUtils;
import net.barribob.boss.mob.mobs.void_blossom.VoidBlossomEntity;
import net.barribob.boss.render.NodeBossBarRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
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
    private static Identifier BARS_TEXTURE;
    private static final NodeBossBarRenderer lichBossBarRenderer =
            new NodeBossBarRenderer(Entities.INSTANCE.getLICH().getTranslationKey(),
            LichUtils.INSTANCE.getHpPercentRageModes(),
                    Mod.INSTANCE.identifier("textures/gui/lich_boss_bar_dividers.png"),
                    LichUtils.textureSize);
    private static final NodeBossBarRenderer voidBlossomBarRenderer =
            new NodeBossBarRenderer(Entities.INSTANCE.getVOID_BLOSSOM().getTranslationKey(),
                    VoidBlossomEntity.Companion.getHpMilestones(),
                    Mod.INSTANCE.identifier("textures/gui/void_blossom_boss_bar_dividers.png"),
                    LichUtils.textureSize);

    @Inject(
            method = "renderBossBar*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomBossBar(DrawContext drawContext, int x, int y, BossBar bossBar, CallbackInfo ci) {
        lichBossBarRenderer.renderBossBar(BARS_TEXTURE, drawContext, x, y, bossBar, ci);
        voidBlossomBarRenderer.renderBossBar(BARS_TEXTURE, drawContext, x, y, bossBar, ci);
        ObsidilithUtils.INSTANCE.getObsidilithBossBarRenderer().renderBossBar(BARS_TEXTURE, drawContext, x, y, bossBar, ci);
    }
}
