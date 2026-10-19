package com.silahmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.silahmod.SilahMod;
import com.silahmod.entity.BuggyEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Basit sarı buggy modeli (gövde + tekerlekler + roll cage).
 * Görseldeki PUBG buggy'ye yaklaşık kutu geometrisi.
 */
public class BuggyRenderer extends EntityRenderer<BuggyEntity> {
	private static final Identifier TEXTURE = SilahMod.id("textures/entity/buggy.png");
	private final BuggyModel model;

	public BuggyRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.9F;
		this.model = new BuggyModel(BuggyModel.createBodyLayer().bakeRoot());
	}

	@Override
	public void render(BuggyEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.0, 0.0);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
		// Hafif sarsıntı hızdayken
		float speed = entity.getCurrentSpeed();
		if (Math.abs(speed) > 0.05F) {
			float shake = Mth.sin((entity.tickCount + partialTicks) * 0.8F) * 0.02F * Math.abs(speed) * 4.0F;
			poseStack.translate(0.0, shake, 0.0);
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0, -1.5, 0.0);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	@Override
	public Identifier getTextureLocation(BuggyEntity entity) {
		return TEXTURE;
	}

	/** Basit kutu model: gövde, 4 teker, roll-cage çubukları. */
	public static class BuggyModel extends EntityModel<BuggyEntity> {
		private final ModelPart root;

		public BuggyModel(ModelPart root) {
			super(RenderType::entityCutoutNoCull);
			this.root = root;
		}

		public static LayerDefinition createBodyLayer() {
			MeshDefinition mesh = new MeshDefinition();
			PartDefinition root = mesh.getRoot();

			// Ana gövde (sarı)
			root.addOrReplaceChild("body",
					CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, -10.0F, 14.0F, 5.0F, 18.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));

			// Ön kaput
			root.addOrReplaceChild("hood",
					CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -14.0F, 12.0F, 3.5F, 5.0F),
					PartPose.offset(0.0F, 13.0F, 0.0F));

			// Koltuk arkası
			root.addOrReplaceChild("seat_back",
					CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, 4.0F, 10.0F, 6.0F, 2.0F),
					PartPose.offset(0.0F, 10.0F, 0.0F));

			// Roll cage dikmeleri
			root.addOrReplaceChild("cage_l",
					CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -8.0F, -2.0F, 1.0F, 10.0F, 1.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));
			root.addOrReplaceChild("cage_r",
					CubeListBuilder.create().texOffs(0, 0).addBox(5.5F, -8.0F, -2.0F, 1.0F, 10.0F, 1.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));
			root.addOrReplaceChild("cage_top",
					CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -8.0F, -2.0F, 13.0F, 1.0F, 1.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));
			root.addOrReplaceChild("cage_rear",
					CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -8.0F, 5.0F, 13.0F, 1.0F, 1.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));
			root.addOrReplaceChild("cage_side_l",
					CubeListBuilder.create().texOffs(0, 0).addBox(-6.5F, -8.0F, -2.0F, 1.0F, 1.0F, 8.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));
			root.addOrReplaceChild("cage_side_r",
					CubeListBuilder.create().texOffs(0, 0).addBox(5.5F, -8.0F, -2.0F, 1.0F, 1.0F, 8.0F),
					PartPose.offset(0.0F, 12.0F, 0.0F));

			// Tekerlekler (koyu)
			float wheelY = 16.5F;
			root.addOrReplaceChild("wheel_fl",
					CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 3.0F),
					PartPose.offset(-7.5F, wheelY, -8.0F));
			root.addOrReplaceChild("wheel_fr",
					CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 3.0F),
					PartPose.offset(7.5F, wheelY, -8.0F));
			root.addOrReplaceChild("wheel_bl",
					CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 3.0F),
					PartPose.offset(-7.5F, wheelY, 7.0F));
			root.addOrReplaceChild("wheel_br",
					CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 3.0F),
					PartPose.offset(7.5F, wheelY, 7.0F));

			return LayerDefinition.create(mesh, 16, 16);
		}

		@Override
		public void setupAnim(BuggyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			// Statik model
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
				int color) {
			this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
		}
	}
}
