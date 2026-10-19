package com.silahmod.item;

import com.silahmod.ModEntities;
import com.silahmod.entity.BuggyEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Menüden alınan buggy item'ı. Sağ tık ile yere bırakılır, entity spawn olur.
 */
public class BuggyItem extends Item {
	public BuggyItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockPos spawnPos = pos.relative(face);

		// Yüzeye yakın spawn
		Vec3 center = Vec3.atBottomCenterOf(spawnPos);
		if (face == Direction.UP) {
			center = new Vec3(center.x, pos.getY() + 1.0, center.z);
		}

		BuggyEntity buggy = ModEntities.BUGGY.create(serverLevel);
		if (buggy == null) {
			return InteractionResult.FAIL;
		}

		buggy.setPos(center.x, center.y, center.z);
		float yaw = context.getHorizontalDirection().toYRot();
		buggy.setYRot(yaw);
		buggy.setHealth(BuggyEntity.MAX_HEALTH);

		serverLevel.addFreshEntity(buggy);
		serverLevel.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, buggy.position());
		serverLevel.playSound(null, buggy.getX(), buggy.getY(), buggy.getZ(),
				SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 0.8F, 1.1F);

		if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.SUCCESS;
	}
}
