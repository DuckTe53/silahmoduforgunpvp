package com.silahmod.entity;

import com.silahmod.SilahMod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * PUBG tarzı sarı buggy. Menüden item olarak alınır, sağ tık ile spawn edilir.
 * 200 can (100 kalp). Öldürülünce patlar. W basılı tutuldukça hız orta derecede artar.
 */
public class BuggyEntity extends Entity {
	public static final float MAX_HEALTH = 200.0F;
	private static final float MAX_SPEED = 0.55F;       // blok/tick civarı (orta-hızlı)
	private static final float ACCEL = 0.018F;          // W basılıyken ivme
	private static final float DECEL = 0.92F;           // gaz bırakınca yavaşlama
	private static final float TURN_SPEED = 2.8F;       // derece/tick

	private static final EntityDataAccessor<Float> DATA_HEALTH =
			SynchedEntityData.defineId(BuggyEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_SPEED =
			SynchedEntityData.defineId(BuggyEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_THROTTLE =
			SynchedEntityData.defineId(BuggyEntity.class, EntityDataSerializers.BOOLEAN);

	private float inputForward; // -1..1 (sunucu tarafı, client paketinden)
	private float inputStrafe;
	private int invulnTicks;

	public BuggyEntity(EntityType<? extends BuggyEntity> type, Level level) {
		super(type, level);
		this.setMaxUpStep(1.0F);
		this.blocksBuilding = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_HEALTH, MAX_HEALTH);
		builder.define(DATA_SPEED, 0.0F);
		builder.define(DATA_THROTTLE, false);
	}

	public float getHealth() {
		return this.entityData.get(DATA_HEALTH);
	}

	public void setHealth(float health) {
		this.entityData.set(DATA_HEALTH, Mth.clamp(health, 0.0F, MAX_HEALTH));
	}

	public float getCurrentSpeed() {
		return this.entityData.get(DATA_SPEED);
	}

	public void setCurrentSpeed(float speed) {
		this.entityData.set(DATA_SPEED, speed);
	}

	public boolean isThrottle() {
		return this.entityData.get(DATA_THROTTLE);
	}

	public void setThrottle(boolean throttle) {
		this.entityData.set(DATA_THROTTLE, throttle);
	}

	/** Client'tan gelen kontrol girdisi (ileri/geri + sağ/sol). */
	public void setInput(float forward, float strafe) {
		this.inputForward = Mth.clamp(forward, -1.0F, 1.0F);
		this.inputStrafe = Mth.clamp(strafe, -1.0F, 1.0F);
		setThrottle(this.inputForward > 0.05F);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (!this.level().isClientSide && !player.isSecondaryUseActive()) {
			if (this.getPassengers().isEmpty()) {
				player.startRiding(this);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().isEmpty();
	}

	@Override
	public LivingEntity getControllingPassenger() {
		Entity first = this.getFirstPassenger();
		return first instanceof LivingEntity living ? living : null;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.invulnTicks > 0) {
			this.invulnTicks--;
		}

		if (this.getHealth() <= 0.0F) {
			explodeAndDie();
			return;
		}

		// Yerçekimi
		if (!this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
		}

		LivingEntity controller = this.getControllingPassenger();
		if (controller instanceof Player player && !this.level().isClientSide) {
			// Client paketi gelmezse (ilk tick vs) input sıfır kalır; throttle bayrağı client'tan gelir
			float forward = this.inputForward;
			float strafe = this.inputStrafe;

			// Dönüş
			if (Math.abs(strafe) > 0.01F) {
				float turn = -strafe * TURN_SPEED * (forward >= 0 ? 1.0F : -0.6F);
				this.setYRot(this.getYRot() + turn);
			}

			// Hız
			float speed = getCurrentSpeed();
			if (forward > 0.05F) {
				speed = Math.min(MAX_SPEED, speed + ACCEL * forward);
			} else if (forward < -0.05F) {
				speed = Math.max(-MAX_SPEED * 0.4F, speed + ACCEL * forward * 0.6F);
			} else {
				speed *= DECEL;
				if (Math.abs(speed) < 0.005F) {
					speed = 0.0F;
				}
			}
			setCurrentSpeed(speed);

			// Yön vektörü
			float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
			double dx = -Mth.sin(yawRad) * speed;
			double dz = Mth.cos(yawRad) * speed;
			this.setDeltaMovement(dx, this.getDeltaMovement().y, dz);

			// Her tick input'u biraz sönümle (paket gelmezse durur)
			this.inputForward *= 0.7F;
			this.inputStrafe *= 0.7F;
		} else if (!this.level().isClientSide) {
			// Sürücü yoksa yavaşla
			float speed = getCurrentSpeed() * DECEL;
			if (Math.abs(speed) < 0.005F) speed = 0.0F;
			setCurrentSpeed(speed);
			float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
			this.setDeltaMovement(-Mth.sin(yawRad) * speed, this.getDeltaMovement().y, Mth.cos(yawRad) * speed);
			setThrottle(false);
		}

		this.move(MoverType.SELF, this.getDeltaMovement());

		// Sürtünme (yerde)
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.96, 1.0, 0.96));
		} else {
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
		}

		// Yolcu pozisyonu
		if (!this.getPassengers().isEmpty()) {
			this.positionRider(this.getFirstPassenger());
		}
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
		if (this.hasPassenger(passenger)) {
			double yOffset = this.getBbHeight() * 0.55;
			Vec3 pos = new Vec3(0.0, yOffset, 0.15).yRot(-this.getYRot() * Mth.DEG_TO_RAD);
			moveFunction.accept(passenger, this.getX() + pos.x, this.getY() + pos.y, this.getZ() + pos.z);
			passenger.setYRot(this.getYRot());
			passenger.setYHeadRot(this.getYRot());
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (this.isInvulnerableTo(level, source) || this.invulnTicks > 0) {
			return false;
		}
		float newHealth = getHealth() - amount;
		setHealth(newHealth);
		this.invulnTicks = 8;
		this.markHurt();
		this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

		level.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.NEUTRAL, 1.0F, 0.8F);

		if (newHealth <= 0.0F) {
			explodeAndDie();
		}
		return true;
	}

	private void explodeAndDie() {
		if (this.level().isClientSide) {
			return;
		}
		Level level = this.level();
		// Yolcuları indir
		this.ejectPassengers();

		level.explode(this, this.getX(), this.getY() + 0.5, this.getZ(),
				3.5F, false, Level.ExplosionInteraction.MOB);

		if (level instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 30; i++) {
				serverLevel.sendParticles(ParticleTypes.FLAME,
						this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
						this.getY() + this.random.nextDouble() * 1.5,
						this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
						1, 0.0, 0.05, 0.0, 0.02);
			}
			serverLevel.sendParticles(ParticleTypes.EXPLOSION,
					this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
		}

		this.discard();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.contains("Health")) {
			setHealth(tag.getFloat("Health"));
		}
		if (tag.contains("Speed")) {
			setCurrentSpeed(tag.getFloat("Speed"));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putFloat("Health", getHealth());
		tag.putFloat("Speed", getCurrentSpeed());
	}

	@Override
	public EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
		return EntityDimensions.scalable(1.8F, 1.1F);
	}
}
