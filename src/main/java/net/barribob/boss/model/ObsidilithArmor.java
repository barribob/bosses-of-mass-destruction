package net.barribob.boss.model;

import net.barribob.boss.mob.mobs.obsidilith.ObsidilithEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
// Made with Blockbench 3.7.5

public class ObsidilithArmor extends EntityModel<ObsidilithEntity> {
    private final ModelPart shell;
    private final ModelPart shell1;
    private final ModelPart shell2;
    private final ModelPart body2;
    private final ModelPart bb_main;

    public ObsidilithArmor() {
        textureWidth = 256;
        textureHeight = 256;

        shell = new ModelPart(this);
        shell.setPivot(0.0F, 24.0F, 0.0F);


        shell1 = new ModelPart(this);
        shell1.setPivot(-13.5F, 0.0F, 0.0F);
        shell.addChild(shell1);
        shell1.setTextureOffset(60, 172).addCuboid(-4.5F, -68.0F, -12.0F, 9.0F, 61.0F, 24.0F, 0.0F, false);

        shell2 = new ModelPart(this);
        shell2.setPivot(13.5F, 0.0F, 0.0F);
        shell.addChild(shell2);
        shell2.setTextureOffset(134, 0).addCuboid(-4.5F, -68.0F, -12.0F, 9.0F, 61.0F, 24.0F, 0.0F, false);

        body2 = new ModelPart(this);
        body2.setPivot(7.0F, 0.0F, 0.0F);
        shell.addChild(body2);
        body2.setTextureOffset(172, 81).addCuboid(-4.0F, -66.0F, -9.0F, 8.0F, 59.0F, 18.0F, 0.0F, false);
        body2.setTextureOffset(172, 172).addCuboid(-19.0F, -66.0F, -9.0F, 9.0F, 59.0F, 18.0F, 0.0F, false);
        body2.setTextureOffset(0, 122).addCuboid(-12.0F, -71.0F, -12.0F, 10.0F, 64.0F, 24.0F, 0.0F, false);

        bb_main = new ModelPart(this);
        bb_main.setPivot(0.0F, 24.0F, 0.0F);
        bb_main.setTextureOffset(1, 88).addCuboid(-20.0F, -9.0F, -14.0F, 40.0F, 10.0F, 28.0F, 0.0F, false);
    }

    @Override
    public void setAngles(ObsidilithEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        shell.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        bb_main.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}