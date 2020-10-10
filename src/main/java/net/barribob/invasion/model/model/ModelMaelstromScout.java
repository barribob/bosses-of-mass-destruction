// Made with Blockbench 3.6.6
// Exported for Minecraft version 1.12.2 or 1.15.2 (same format for both) for entity models animated with GeckoLib
// Paste this class into your mod and follow the documentation for GeckoLib to use animations. You can find the documentation here: https://github.com/bernie-g/geckolib
// Blockbench plugin created by Gecko
package net.barribob.invasion.model.model;

import net.barribob.invasion.Invasions;
import net.barribob.invasion.mob.MaelstromScoutEntity;
import software.bernie.geckolib.animation.model.AnimatedEntityModel;
import software.bernie.geckolib.animation.render.AnimatedModelRenderer;
import software.bernie.geckolib.forgetofabric.ResourceLocation;

public class ModelMaelstromScout extends AnimatedEntityModel<MaelstromScoutEntity> {

    private final AnimatedModelRenderer root;
	private final AnimatedModelRenderer wisps;
	private final AnimatedModelRenderer body;
	private final AnimatedModelRenderer rightArm;
	private final AnimatedModelRenderer rightForearm;
	private final AnimatedModelRenderer rightHand;
	private final AnimatedModelRenderer rightCuff;
	private final AnimatedModelRenderer rightShoulderpad;
	private final AnimatedModelRenderer leftArm;
	private final AnimatedModelRenderer leftForearm;
	private final AnimatedModelRenderer leftHand;
	private final AnimatedModelRenderer sword;
	private final AnimatedModelRenderer leftCuff;
	private final AnimatedModelRenderer leftShoulderpad;
	private final AnimatedModelRenderer head;
	private final AnimatedModelRenderer headFrill;

    public ModelMaelstromScout()
    {
        textureWidth = 64;
		textureHeight = 64;
		root = new AnimatedModelRenderer(this);
		root.setRotationPoint(0.0F, 0.0F, 0.0F);
		
		root.setModelRendererName("root");
		this.registerModelRenderer(root);

		wisps = new AnimatedModelRenderer(this);
		wisps.setRotationPoint(0.0F, 12.0F, 0.0F);
		root.addChild(wisps);
		wisps.setTextureOffset(30, 0).addBox(-3.5F, 2.0F, -2.0F, 7.0F, 8.0F, 4.0F, 0.0F, false);
		wisps.setModelRendererName("wisps");
		this.registerModelRenderer(wisps);

		body = new AnimatedModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		wisps.addChild(body);
		body.setTextureOffset(0, 0).addBox(-4.5F, -12.0F, -3.0F, 9.0F, 14.0F, 6.0F, 0.0F, false);
		body.setModelRendererName("body");
		this.registerModelRenderer(body);

		rightArm = new AnimatedModelRenderer(this);
		rightArm.setRotationPoint(4.5F, -10.0F, 0.0F);
		body.addChild(rightArm);
		rightArm.setTextureOffset(0, 36).addBox(1.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);
		rightArm.setModelRendererName("rightArm");
		this.registerModelRenderer(rightArm);

		rightForearm = new AnimatedModelRenderer(this);
		rightForearm.setRotationPoint(2.5F, 4.0F, 0.0F);
		rightArm.addChild(rightForearm);
		rightForearm.setTextureOffset(36, 28).addBox(-1.51F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, 0.0F, false);
		rightForearm.setModelRendererName("rightForearm");
		this.registerModelRenderer(rightForearm);

		rightHand = new AnimatedModelRenderer(this);
		rightHand.setRotationPoint(0.0F, 4.0F, 0.0F);
		rightForearm.addChild(rightHand);
		rightHand.setTextureOffset(0, 49).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		rightHand.setModelRendererName("rightHand");
		this.registerModelRenderer(rightHand);

		rightCuff = new AnimatedModelRenderer(this);
		rightCuff.setRotationPoint(2.5F, -4.0F, -1.0F);
		rightForearm.addChild(rightCuff);
		rightCuff.setTextureOffset(36, 18).addBox(-4.5F, 6.0F, -1.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		rightCuff.setModelRendererName("rightCuff");
		this.registerModelRenderer(rightCuff);

		rightShoulderpad = new AnimatedModelRenderer(this);
		rightShoulderpad.setRotationPoint(5.0F, 0.0F, -1.0F);
		rightArm.addChild(rightShoulderpad);
		rightShoulderpad.setTextureOffset(24, 20).addBox(-4.5F, -2.5F, -1.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);
		rightShoulderpad.setModelRendererName("rightShoulderpad");
		this.registerModelRenderer(rightShoulderpad);

		leftArm = new AnimatedModelRenderer(this);
		leftArm.setRotationPoint(-4.5F, -10.0F, 0.0F);
		body.addChild(leftArm);
		leftArm.setTextureOffset(9, 42).addBox(-4.0F, -2.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);
		leftArm.setModelRendererName("leftArm");
		this.registerModelRenderer(leftArm);

		leftForearm = new AnimatedModelRenderer(this);
		leftForearm.setRotationPoint(-2.5F, 4.0F, 0.0F);
		leftArm.addChild(leftForearm);
		leftForearm.setTextureOffset(42, 42).addBox(-1.51F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, 0.0F, false);
		leftForearm.setModelRendererName("leftForearm");
		this.registerModelRenderer(leftForearm);

		leftHand = new AnimatedModelRenderer(this);
		leftHand.setRotationPoint(0.0F, 4.0F, 0.0F);
		leftForearm.addChild(leftHand);
		leftHand.setTextureOffset(0, 49).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		leftHand.setModelRendererName("leftHand");
		this.registerModelRenderer(leftHand);

		sword = new AnimatedModelRenderer(this);
		sword.setRotationPoint(0.5F, 2.5F, -0.625F);
		leftHand.addChild(sword);
		sword.setTextureOffset(24, 36).addBox(-0.5F, -0.5F, -3.375F, 1.0F, 1.0F, 4.0F, 0.0F, false);
		sword.setTextureOffset(24, 36).addBox(-1.0F, -2.5F, -4.375F, 2.0F, 5.0F, 1.0F, 0.0F, false);
		sword.setTextureOffset(24, 34).addBox(-0.5F, -1.0F, -13.375F, 1.0F, 2.0F, 9.0F, 0.0F, false);
		sword.setTextureOffset(24, 36).addBox(-0.5F, -0.5F, -14.375F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		sword.setModelRendererName("sword");
		this.registerModelRenderer(sword);

		leftCuff = new AnimatedModelRenderer(this);
		leftCuff.setRotationPoint(2.5F, -4.0F, -1.0F);
		leftForearm.addChild(leftCuff);
		leftCuff.setTextureOffset(36, 23).addBox(-4.5F, 6.0F, -1.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
		leftCuff.setModelRendererName("leftCuff");
		this.registerModelRenderer(leftCuff);

		leftShoulderpad = new AnimatedModelRenderer(this);
		leftShoulderpad.setRotationPoint(0.0F, 0.0F, -1.0F);
		leftArm.addChild(leftShoulderpad);
		leftShoulderpad.setTextureOffset(30, 12).addBox(-4.5F, -2.5F, -1.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);
		leftShoulderpad.setModelRendererName("leftShoulderpad");
		this.registerModelRenderer(leftShoulderpad);

		head = new AnimatedModelRenderer(this);
		head.setRotationPoint(0.0F, -14.0F, 0.0F);
		body.addChild(head);
		head.setTextureOffset(0, 20).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		head.setModelRendererName("head");
		this.registerModelRenderer(head);

		headFrill = new AnimatedModelRenderer(this);
		headFrill.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(headFrill);
		headFrill.setTextureOffset(23, 27).addBox(-1.0F, -7.1F, -5.1F, 2.0F, 9.0F, 9.0F, 0.0F, false);
		headFrill.setModelRendererName("headFrill");
		this.registerModelRenderer(headFrill);

    this.rootBones.add(root);
  }


    @Override
    public ResourceLocation getAnimationFileLocation()
    {
        return new ResourceLocation(Invasions.MODID, "animations/scout.json");
    }
}