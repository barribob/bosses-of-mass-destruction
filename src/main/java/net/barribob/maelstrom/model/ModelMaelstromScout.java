package net.barribob.maelstrom.model;

// Made with Blockbench 3.5.4

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class ModelMaelstromScout<T extends Entity> extends EntityModel<T> {
    private final ModelPart root;

    @SuppressWarnings("unused")
    public ModelMaelstromScout() {
        textureWidth = 64;
        textureHeight = 64;

        root = new ModelPart(this);
        root.setPivot(0.0F, 0.0F, 0.0F);

        ModelPart wisps = new ModelPart(this);
        wisps.setPivot(0.0F, 12.0F, 0.0F);
        root.addChild(wisps);
        wisps.setTextureOffset(30, 0).addCuboid(-3.5F, 2.0F, -2.0F, 7.0F, 8.0F, 4.0F, 0.0F, false);

        ModelPart body = new ModelPart(this);
        body.setPivot(0.0F, 0.0F, 0.0F);
        wisps.addChild(body);
        body.setTextureOffset(0, 0).addCuboid(-4.5F, -12.0F, -3.0F, 9.0F, 14.0F, 6.0F, 0.0F, false);

        ModelPart rightArm = new ModelPart(this);
        rightArm.setPivot(4.5F, -10.0F, 0.0F);
        body.addChild(rightArm);
        rightArm.setTextureOffset(0, 36).addCuboid(1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        ModelPart rightForearm = new ModelPart(this);
        rightForearm.setPivot(2.5F, 4.0F, 0.0F);
        rightArm.addChild(rightForearm);
        rightForearm.setTextureOffset(36, 28).addCuboid(-1.51F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, 0.0F, false);

        ModelPart rightHand = new ModelPart(this);
        rightHand.setPivot(0.0F, 4.0F, 0.0F);
        rightForearm.addChild(rightHand);
        rightHand.setTextureOffset(0, 49).addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        ModelPart rightCuff = new ModelPart(this);
        rightCuff.setPivot(2.5F, -4.0F, -1.0F);
        rightForearm.addChild(rightCuff);
        rightCuff.setTextureOffset(36, 18).addCuboid(-4.5F, 6.0F, -1.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);

        ModelPart rightShoulderpad = new ModelPart(this);
        rightShoulderpad.setPivot(5.0F, 0.0F, -1.0F);
        rightArm.addChild(rightShoulderpad);
        rightShoulderpad.setTextureOffset(24, 20).addCuboid(-4.5F, -2.5F, -1.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);

        ModelPart leftArm = new ModelPart(this);
        leftArm.setPivot(-4.5F, -10.0F, 0.0F);
        body.addChild(leftArm);
        leftArm.setTextureOffset(9, 42).addCuboid(-4.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        ModelPart leftForearm = new ModelPart(this);
        leftForearm.setPivot(-2.5F, 4.0F, 0.0F);
        leftArm.addChild(leftForearm);
        leftForearm.setTextureOffset(42, 42).addCuboid(-1.51F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, 0.0F, false);

        ModelPart leftHand = new ModelPart(this);
        leftHand.setPivot(0.0F, 4.0F, 0.0F);
        leftForearm.addChild(leftHand);
        leftHand.setTextureOffset(0, 49).addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);

        ModelPart sword = new ModelPart(this);
        sword.setPivot(0.5F, 2.5F, -0.625F);
        leftHand.addChild(sword);
        sword.setTextureOffset(24, 36).addCuboid(-0.5F, -0.5F, -3.375F, 1.0F, 1.0F, 4.0F, 0.0F, false);
        sword.setTextureOffset(24, 36).addCuboid(-1.0F, -2.5F, -4.375F, 2.0F, 5.0F, 1.0F, 0.0F, false);
        sword.setTextureOffset(24, 34).addCuboid(-0.5F, -1.0F, -13.375F, 1.0F, 2.0F, 9.0F, 0.0F, false);
        sword.setTextureOffset(24, 36).addCuboid(-0.5F, -0.5F, -14.375F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        ModelPart leftCuff = new ModelPart(this);
        leftCuff.setPivot(2.5F, -4.0F, -1.0F);
        leftForearm.addChild(leftCuff);
        leftCuff.setTextureOffset(36, 23).addCuboid(-4.5F, 6.0F, -1.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);

        ModelPart leftShoulderpad = new ModelPart(this);
        leftShoulderpad.setPivot(0.0F, 0.0F, -1.0F);
        leftArm.addChild(leftShoulderpad);
        leftShoulderpad.setTextureOffset(30, 12).addCuboid(-4.5F, -2.5F, -1.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);

        ModelPart head = new ModelPart(this);
        head.setPivot(0.0F, -14.0F, 0.0F);
        body.addChild(head);
        head.setTextureOffset(0, 20).addCuboid(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        ModelPart headFrill = new ModelPart(this);
        headFrill.setPivot(0.0F, 0.0F, 0.0F);
        head.addChild(headFrill);
        headFrill.setTextureOffset(23, 27).addCuboid(-1.0F, -7.1F, -5.1F, 2.0F, 9.0F, 9.0F, 0.0F, false);
    }

    @Override
    public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        //previously the render function, render code was moved to a method below
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}