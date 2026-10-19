package com.silahmod;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Istemci -> sunucu paketleri. */
public final class Net {
	private Net() {}

	/** Sol tik: ates et. */
	public record ShootPayload() implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ShootPayload> TYPE = new CustomPacketPayload.Type<>(SilahMod.id("shoot"));
		public static final StreamCodec<FriendlyByteBuf, ShootPayload> CODEC = StreamCodec.unit(new ShootPayload());

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** R: sarjor doldur. */
	public record ReloadPayload() implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ReloadPayload> TYPE = new CustomPacketPayload.Type<>(SilahMod.id("reload"));
		public static final StreamCodec<FriendlyByteBuf, ReloadPayload> CODEC = StreamCodec.unit(new ReloadPayload());

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** B menusu: esya al (siralama icin bkz. ModItems.byIndex). */
	public record GiveWeaponPayload(int index) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<GiveWeaponPayload> TYPE = new CustomPacketPayload.Type<>(SilahMod.id("give_weapon"));
		public static final StreamCodec<FriendlyByteBuf, GiveWeaponPayload> CODEC =
				ByteBufCodecs.VAR_INT.map(GiveWeaponPayload::new, GiveWeaponPayload::index).cast();

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Buggy sürüş kontrolleri: forward (-1..1), strafe (-1..1). */
	public record VehicleControlPayload(float forward, float strafe) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<VehicleControlPayload> TYPE =
				new CustomPacketPayload.Type<>(SilahMod.id("vehicle_control"));
		public static final StreamCodec<FriendlyByteBuf, VehicleControlPayload> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.FLOAT, VehicleControlPayload::forward,
						ByteBufCodecs.FLOAT, VehicleControlPayload::strafe,
						VehicleControlPayload::new);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(ShootPayload.TYPE, ShootPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ReloadPayload.TYPE, ReloadPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(GiveWeaponPayload.TYPE, GiveWeaponPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(VehicleControlPayload.TYPE, VehicleControlPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ShootPayload.TYPE,
				(payload, context) -> Combat.shoot(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.TYPE,
				(payload, context) -> Combat.reload(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(GiveWeaponPayload.TYPE,
				(payload, context) -> Combat.giveWeapon(context.player(), payload.index()));
		ServerPlayNetworking.registerGlobalReceiver(VehicleControlPayload.TYPE, (payload, context) -> {
			var player = context.player();
			if (player.getVehicle() instanceof com.silahmod.entity.BuggyEntity buggy) {
				buggy.setInput(payload.forward(), payload.strafe());
			}
		});
	}
}
