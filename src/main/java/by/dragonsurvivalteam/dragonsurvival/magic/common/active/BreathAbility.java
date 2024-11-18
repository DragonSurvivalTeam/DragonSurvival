package by.dragonsurvivalteam.dragonsurvival.magic.common.active;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.ISecondAnimation;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BreathAbility extends ChannelingCastAbility implements ISecondAnimation {
	@ConfigRange(min = 0, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = "magic", key = "baseBreathRange", comment = "The base range of the breath attack (breath range increases with dragon growth)")
	public static Integer baseBreathRange = 3;

	public int currentBreathRange;

	public float yaw;
	public float pitch;
	public float speed;
	public float spread;

	public float xComp;
	public float yComp;
	public float zComp;

	public double dx;
	public double dy;
	public double dz;

	public static List<LivingEntity> getEntityLivingBaseNearby(final LivingEntity source, double radius) {
		return getEntitiesNearby(source, LivingEntity.class, radius);
	}

	public static <T extends Entity> List<T> getEntitiesNearby(final LivingEntity source, final Class<T> entityClass, double radius) {
		return source.level.getEntitiesOfClass(entityClass, source.getBoundingBox().inflate(radius, radius, radius), entity -> entity != source && source.distanceTo(entity) <= radius + entity.getBbWidth() / 2f && entity.getY() <= source.getY() + radius);
	}

	@Override
	public boolean requiresStationaryCasting() {
		return false;
	}

	@Override
	public boolean canCastSkill(final Player player) {
		if (ServerFlightHandler.isGliding(player)) {
			return false;
		}

		return super.canCastSkill(player);
	}

	@Override
	public void onCharging(Player player, int currentChargeTime) { /* Nothing to do */ }

	@Override
	public void onChanneling(final Player player, int castDuration) {
		DragonStateHandler handler = DragonUtils.getHandler(player);

		currentBreathRange = calculateCurrentBreathRange(handler.getSize());

		yaw = (float) Math.toRadians(-player.getYRot());
		pitch = (float) Math.toRadians(-player.getXRot());
		speed = calculateCurrentBreathSpeed(handler.getSize());
		spread = calculateSpread(handler.getSize());

		xComp = (float)(Math.sin(yaw) * Math.cos(pitch));
		yComp = (float)Math.sin(pitch);
		zComp = (float)(Math.cos(yaw) * Math.cos(pitch));

		Vec3 eyePos = player.getEyePosition();
		Vec3 lookAngle = player.getLookAngle();
		Vec3 forward;
		Vec3 breathPos;

		double size = handler.getSize();

		if (player.getAbilities().flying) {
			forward = lookAngle.scale(2.0F);
			breathPos = eyePos.add(forward).add(0F, -0.1-0.5F*(size / 30F), 0F);
		} else {
			forward = lookAngle.scale(1.0F);
			breathPos = eyePos.add(forward).add(0F, -0.1F-0.2F*(size / 30F), 0F);
		}

		dx = breathPos.x;
		dy = breathPos.y;
		dz = breathPos.z;
	}


	@Override
	public AbilityAnimation getLoopingAnimation() {
		return new AbilityAnimation("breath", false, false);
	}

	public void hitEntities() {
		AABB breathArea = DragonAbilities.calculateBreathArea(player, currentBreathRange);

		List<Entity> entities = player.level.getEntities(player, breathArea, entity -> {
					// TODO :: Check if solid blocks are between the player and the entity?
					if (entity instanceof LivingEntity livingEntity) {
						// TODO :: Also check for creative? Or add spectator and creative as checks to isValidTarget?
						if (!livingEntity.isSpectator() && livingEntity.isAlive() && TargetingFunctions.isValidTarget(getPlayer(), livingEntity)) {
							if (/* Specific check per ability */ canHitEntity(livingEntity)) {
								// Only allow 1 player hit per second
								return livingEntity.getLastHurtByMob() != player || livingEntity.getLastHurtByMobTimestamp() + Functions.secondsToTicks(1) >= livingEntity.tickCount;
							}
						}
					}

					return false;
				}
		);

		entities.forEach(entity -> {
			if (entity instanceof LivingEntity livingEntity) {
				onEntityHit(livingEntity);
			}
		});
	}

	public abstract boolean canHitEntity(LivingEntity entity);

	public void onEntityHit(final LivingEntity entityHit) {
		if (TargetingFunctions.attackTargets(getPlayer(), entity -> entity.hurt(new BreathDamage(player), getDamage()), entityHit)) {
			entityHit.setDeltaMovement(entityHit.getDeltaMovement().multiply(0.25, 1, 0.25));
			onDamage(entityHit);
		}
	}

	public abstract void onDamage(LivingEntity entity);

	public abstract float getDamage();

	public List<LivingEntity> getEntityLivingBaseNearby(double distanceX, double distanceY, double distanceZ, double radius) {
		return getEntitiesNearby(LivingEntity.class, distanceX, distanceY, distanceZ, radius);
	}

	public <T extends Entity> List<T> getEntitiesNearby(final Class<T> entityClass, double dX, double dY, double dZ, double radius) {
		return player.level.getEntitiesOfClass(entityClass, player.getBoundingBox().inflate(dX, dY, dZ), entity -> entity != player && player.distanceTo(entity) <= radius + entity.getBbWidth() / 2f && entity.getY() <= player.getY() + dY);
	}
	
	public Fluid clipContext() {
		return ClipContext.Fluid.ANY;
	}

	public void hitBlocks() {
		Pair<BlockPos, Direction> data = DragonAbilities.breathStartPosition(player, this, currentBreathRange);
		BlockPos position = data.getFirst();
		Direction direction = data.getSecond();

		if (position == null) {
			return;
		}

		// TODO :: Use the bounding box here as well?

		// Alternative: BlockPos.randomInCube()
		BlockPos.MutableBlockPos mutablePosition = position.mutable();
		for (int x = -(currentBreathRange / 2); x < currentBreathRange / 2; x++) {
			for (int y = -(currentBreathRange / 2); y < currentBreathRange / 2; y++) { // TODO :: Apply eye height for positive y but not negative y?
				for (int z = -(currentBreathRange / 2); z < currentBreathRange / 2; z++) {
					mutablePosition.set(position).move(x, y, z);

					if (mutablePosition.distSqr(position) <= currentBreathRange) {
						BlockState state = player.level.getBlockState(mutablePosition);

						if (state.getBlock() != Blocks.AIR) {
							if (DragonConfigHandler.DRAGON_BREATH_BLOCKS != null && DragonConfigHandler.DRAGON_BREATH_BLOCKS.containsKey(getDragonType().getTypeName()) && DragonConfigHandler.DRAGON_BREATH_BLOCKS.get(getDragonType().getTypeName()).contains(state.getBlock())) {
								if (!player.level.isClientSide) {
									if (player.getRandom().nextFloat() * 100 <= blockBreakChance()) {
										player.level.destroyBlock(mutablePosition, false, player);
										continue;
									}
								}
							}

							onBlock(mutablePosition, state, direction);
						}
					}
				}
			}
		}
	}

	public abstract void onBlock(BlockPos pos, BlockState blockState, Direction direction);

	public int blockBreakChance() {
		return 90;
	}

	@Override
	public int getSortOrder() {
		return 1;
	}

	@Override
	public ArrayList<Component> getInfo() {
		ArrayList<Component> components = new ArrayList<>();

		DragonStateHandler handler = DragonUtils.getHandler(player);
		int range = calculateCurrentBreathRange(handler.getSize());

		components.add(Component.translatable("ds.skill.mana_cost", getInitManaCost()));
		components.add(Component.translatable("ds.skill.channel_cost", getManaCost(), 2));

		components.add(Component.translatable("ds.skill.cast_time", Functions.ticksToSeconds(getSkillChargeTime())));
		components.add(Component.translatable("ds.skill.cooldown", Functions.ticksToSeconds(getSkillCooldown())));

		components.add(Component.translatable("ds.skill.damage", getDamage()));
		components.add(Component.translatable("ds.skill.range.blocks", range));

		if (!Keybind.ABILITY1.isUnbound()) {
			String key = Keybind.ABILITY1.getKey().getDisplayName().getString().toUpperCase(Locale.ROOT);

			if (key.isEmpty()) {
				key = Keybind.ABILITY1.getKey().getDisplayName().getString();
			}

			components.add(Component.translatable("ds.skill.keybind", key));
		}

		return components;
	}

	private static float calculateSpread(double size) {
		return (float) Math.sqrt(size / DragonLevel.ADULT.size) / 5.f + 0.05f;
	}

	public static int calculateNumberOfParticles(double size) {
		return (int) Math.max(Math.min(100, size * 0.6), 12);
	}

	public static class BreathDamage extends EntityDamageSource {
		public BreathDamage(@NotNull final Entity entity) {
			super("player", entity);
			setMagic();
		}
	}

	public static int calculateCurrentBreathRange(double size) {
		float sizeFactor = Math.min((float) size / DragonLevel.ADULT.size, 1.0f);
		float additionalBreathRange = sizeFactor * 4.0f + (float)size * 0.05f;
		return baseBreathRange + (int) additionalBreathRange;
	}

	public static float calculateCurrentBreathSpeed(double size) {
		float sizeFactor = Math.min((float) size / DragonLevel.ADULT.size, 1.0f);
		return sizeFactor * 0.3f + (float)size * 0.004f;
	}
}