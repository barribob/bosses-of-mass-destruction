package net.barribob.maelstrom.mixin;

import net.barribob.maelstrom.MaelstromMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inject into living renderer to render entity animations of any entity that uses the living entity renderer.
 */
@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class AnimationMixins<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;animateModel(Lnet/minecraft/entity/Entity;FFF)V"), method = "render")
    private void animateModel(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        MaelstromMod.INSTANCE.getClientAnimationWatcher().resetModel(this.model);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;isVisible(Lnet/minecraft/entity/LivingEntity;)Z"), method = "render")
    private void afterAnimateModel(T livingEntity, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        MaelstromMod.INSTANCE.getClientAnimationWatcher().setModelRotations(this.model, livingEntity, partialTicks);
    }
}
