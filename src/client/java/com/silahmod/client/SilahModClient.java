package com.silahmod.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.silahmod.ModEntities;
import com.silahmod.Net;
import com.silahmod.SilahMod;
import com.silahmod.client.render.BuggyRenderer;
import com.silahmod.entity.BuggyEntity;
import com.silahmod.item.GunItem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;

public class SilahModClient implements ClientModInitializer {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(SilahMod.id("silahlar"));

	public static KeyMapping reloadKey;
	public static KeyMapping menuKey;

	@Override
	public void onInitializeClient() {
		reloadKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		menuKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

		// Entity renderer
		EntityRendererRegistry.register(ModEntities.BUGGY, BuggyRenderer::new);

		// R = doldur, B = silah menusu + buggy kontrol
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.getConnection() == null) {
				return;
			}
			while (reloadKey.consumeClick()) {
				if (client.player.getMainHandItem().getItem() instanceof GunItem) {
					ClientPlayNetworking.send(new Net.ReloadPayload());
				}
			}
			while (menuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new WeaponMenuScreen());
				}
			}

			// Buggy sürüş: W/S/A/D girdisini sunucuya gönder
			LocalPlayer player = client.player;
			if (player.getVehicle() instanceof BuggyEntity) {
				float forward = 0.0F;
				float strafe = 0.0F;
				if (client.options.keyUp.isDown()) forward += 1.0F;
				if (client.options.keyDown.isDown()) forward -= 1.0F;
				if (client.options.keyLeft.isDown()) strafe += 1.0F;
				if (client.options.keyRight.isDown()) strafe -= 1.0F;
				ClientPlayNetworking.send(new Net.VehicleControlPayload(forward, strafe));
			}
		});

		// Sol tik: elinde ates edilen silah varsa vanilla saldiri yerine ates et.
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (player.getMainHandItem().getItem() instanceof GunItem gun) {
				boolean pressed = clickCount > 0;
				boolean held = gun.automatic() && client.options.keyAttack.isDown();
				if ((pressed || held) && !player.isSpectator() && client.screen == null) {
					ClientPlayNetworking.send(new Net.ShootPayload());
				}
				return true;
			}
			return false;
		});

		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, SilahMod.id("ammo"), AmmoOverlay::renderAmmo);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, SilahMod.id("medkit"), MedkitOverlay::render);
	}
}
