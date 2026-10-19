package com.silahmod;

import com.silahmod.entity.BuggyEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	private ModEntities() {}

	public static final EntityType<BuggyEntity> BUGGY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			SilahMod.id("buggy"),
			EntityType.Builder.of(BuggyEntity::new, MobCategory.MISC)
					.sized(1.8F, 1.1F)
					.clientTrackingRange(10)
					.updateInterval(2)
					.build(ResourceKey.create(Registries.ENTITY_TYPE, SilahMod.id("buggy")))
	);

	public static void init() {
		// no-op
	}
}
