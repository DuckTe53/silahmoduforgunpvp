package com.silahmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.silahmod.SilahMod;
import com.silahmod.entity.BuggyEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 1.21.11 EntityRenderState tabanlı basit buggy renderer.
 * Detaylı model yerine gölge + isim; asıl görünüm için ileride model eklenebilir.
 * Şimdilik entity görünür olsun diye minimal implementasyon.
 */
public class BuggyRenderer extends EntityRenderer<BuggyEntity, BuggyRenderer.BuggyRenderState> {
	private static final Identifier TEXTURE = SilahMod.id("textures/entity/buggy.png");

	public BuggyRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.9F;
	}

	@Override
	public BuggyRenderState createRenderState() {
		return new BuggyRenderState();
	}

	@Override
	public void extractRenderState(BuggyEntity entity, BuggyRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.yRot = entity.getYRot();
		state.speed = entity.getCurrentSpeed();
		state.health = entity.getHealth();
	}

	@Override
	public Identifier getTextureLocation(BuggyRenderState state) {
		return TEXTURE;
	}

	/** Render state: sadece ihtiyaç duyulan alanlar. */
	public static class BuggyRenderState extends EntityRenderState {
		public float yRot;
		public float speed;
		public float health;
	}
}
