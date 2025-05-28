package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class DragonModifiers{
	/** Also used for attack range */
	private static final UUID DRAGON_REACH_MODIFIER = UUID.fromString("7455d5c7-4e1f-4cca-ab46-d79353764020");
	private static final UUID DRAGON_HEALTH_MODIFIER = UUID.fromString("03574e62-f9e4-4f1b-85ad-fde00915e446");
	private static final UUID DRAGON_DAMAGE_MODIFIER = UUID.fromString("5bd3cebc-132e-4f9d-88ef-b686c7ad1e2c");
	private static final UUID DRAGON_SWIM_SPEED_MODIFIER = UUID.fromString("2a9341f3-d19e-446c-924b-7cf2e5259e10");
	private static final UUID DRAGON_STEP_HEIGHT_MODIFIER = UUID.fromString("f3b0b3e3-3b7d-4b1b-8f3d-3b7d4b1b8f3d");
	private static final UUID DRAGON_MOVEMENT_SPEED_MODIFIER = UUID.fromString("a11bba07-27e2-4c98-ac2c-34ae9f9b0694");

	private static final UUID DRAGON_BODY_MOVEMENT_SPEED = UUID.fromString("114fe18b-60fd-4284-b6ce-14d090454402");
	private static final UUID DRAGON_BODY_ARMOR = UUID.fromString("8728438d-c838-4968-9382-efb95a36d72a");
	private static final UUID DRAGON_BODY_STRENGTH = UUID.fromString("f591516e-749d-41f3-ba5a-b94cdf506193");
	private static final UUID DRAGON_BODY_STRENGTH_MULT = UUID.fromString("05a4e0c8-f76b-45db-a244-3d588146a4ab");
	private static final UUID DRAGON_BODY_KNOCKBACK_BONUS = UUID.fromString("f33f6ac0-2dd8-41a6-abe0-37e5cd0a383c");
	private static final UUID DRAGON_BODY_SWIM_SPEED_BONUS = UUID.fromString("244edf11-e535-4dc1-b8a0-4f56a5a80e0c");
	private static final UUID DRAGON_BODY_STEP_HEIGHT_BONUS = UUID.fromString("df2b333e-46c8-4315-a50d-096785e4f592");
	private static final UUID DRAGON_BODY_GRAVITY_MULT = UUID.fromString("de994497-1cca-45e6-b398-a1736f43e5ec");
	private static final UUID DRAGON_BODY_HEALTH_MULT = UUID.fromString("9068f914-511d-44cf-a2a3-0808f7c67326");

	public static AttributeModifier buildHealthMod(double size){
		double healthModifier;
		if(ServerConfig.allowLargeScaling && size > ServerConfig.maxHealthSize) {
			double healthModifierPercentage = Math.min(1.0, (size - ServerConfig.maxHealthSize) / (ServerConfig.maxGrowthSize - DragonLevel.ADULT.size));
			healthModifier = Mth.lerp(healthModifierPercentage, ServerConfig.maxHealth, ServerConfig.largeMaxHealth) - 20;
		}
		else {
			double healthModifierPercentage = Math.min(1.0, (size - DragonLevel.NEWBORN.size) / (ServerConfig.maxHealthSize - DragonLevel.NEWBORN.size));
			healthModifier = Mth.lerp(healthModifierPercentage, ServerConfig.minHealth, ServerConfig.maxHealth) - 20;
		}
		return new AttributeModifier(DRAGON_HEALTH_MODIFIER, "Dragon Health Adjustment", healthModifier, AttributeModifier.Operation.ADDITION);
	}

	public static AttributeModifier buildReachMod(double size){
		double reachModifier;
		if(ServerConfig.allowLargeScaling && size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
			reachModifier = ServerConfig.reachBonus + ServerConfig.largeReachScalar * (size / ServerConfig.DEFAULT_MAX_GROWTH_SIZE);
		}
		else {
			reachModifier = Math.max(ServerConfig.reachBonus, (size - DragonLevel.NEWBORN.size) / (ServerConfig.DEFAULT_MAX_GROWTH_SIZE - DragonLevel.NEWBORN.size) * ServerConfig.reachBonus);
		}
		return new AttributeModifier(DRAGON_REACH_MODIFIER, "Dragon Reach Adjustment", reachModifier, Operation.MULTIPLY_BASE);
	}


	public static AttributeModifier buildDamageMod(DragonStateHandler handler, boolean isDragon){
		double ageBonus = isDragon ? handler.getLevel() == DragonLevel.ADULT ? ServerConfig.adultBonusDamage : handler.getLevel() == DragonLevel.YOUNG ? ServerConfig.youngBonusDamage : ServerConfig.babyBonusDamage : 0;
		if(ServerConfig.allowLargeScaling && handler.getSize() > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
			double damageModPercentage = Math.min(1.0, (handler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / (ServerConfig.maxGrowthSize - ServerConfig.DEFAULT_MAX_GROWTH_SIZE));
			ageBonus = Mth.lerp(damageModPercentage, ageBonus, ServerConfig.largeDamageBonus);
		}
		return new AttributeModifier(DRAGON_DAMAGE_MODIFIER, "Dragon Damage Adjustment", ageBonus, Operation.ADDITION);
	}

	public static AttributeModifier buildSwimSpeedMod(AbstractDragonType dragonType){
		return new AttributeModifier(DRAGON_SWIM_SPEED_MODIFIER, "Dragon Swim Speed Adjustment", Objects.equals(dragonType, DragonTypes.SEA) && ServerConfig.seaSwimmingBonuses ? 1 : 0, Operation.ADDITION);
	}

	public static AttributeModifier buildStepHeightMod(DragonStateHandler handler, double size) {
		double stepHeightBonus = handler.getLevel() == DragonLevel.ADULT ? ServerConfig.adultStepHeight : handler.getLevel() == DragonLevel.YOUNG ? ServerConfig.youngStepHeight : ServerConfig.newbornStepHeight;
		if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE && ServerConfig.allowLargeScaling)  {
			stepHeightBonus += ServerConfig.largeStepHeightScalar * (size - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
		}
		return new AttributeModifier(DRAGON_STEP_HEIGHT_MODIFIER, "Dragon Step Height Adjustment", stepHeightBonus, Operation.ADDITION);
	}

	public static AttributeModifier buildMovementSpeedMod(DragonStateHandler handler, double size) {
		double moveSpeedMultiplier = 1;
		if(handler.getLevel() == DragonLevel.NEWBORN) {
			double youngPercent = Math.min(1.0, (size - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size));
			moveSpeedMultiplier = Mth.lerp(youngPercent, ServerConfig.moveSpeedNewborn, ServerConfig.moveSpeedYoung);
		} else if(handler.getLevel() == DragonLevel.YOUNG) {
			double adultPercent = Math.min(1.0, (size - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size));
			moveSpeedMultiplier = Mth.lerp(adultPercent, ServerConfig.moveSpeedYoung, ServerConfig.moveSpeedAdult);
		} else if(handler.getLevel() == DragonLevel.ADULT) {
			if(ServerConfig.allowLargeScaling && size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
				moveSpeedMultiplier = ServerConfig.moveSpeedAdult + ServerConfig.largeMovementSpeedScalar * (size - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
			} else {
				moveSpeedMultiplier = ServerConfig.moveSpeedAdult;
			}
		}
		return new AttributeModifier(DRAGON_MOVEMENT_SPEED_MODIFIER, "Dragon Movement Speed Adjustment", moveSpeedMultiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	public static void updateModifiers(Player player) {
		updateTypeModifiers(player);
		updateSizeModifiers(player);
		updateBodyModifiers(player);
	}

	public static void updateTypeModifiers(final Player player) {
		if (DragonStateProvider.getCap(player).isPresent()) {
			DragonStateHandler handler = DragonStateProvider.getHandler(player);
			if (handler.isDragon()) {
				// Grant the dragon attribute modifiers
				AttributeModifier swimSpeed = buildSwimSpeedMod(handler.getType());
				updateSwimSpeedModifier(player, swimSpeed);
			} else {
				// Remove the dragon attribute modifiers
				AttributeModifier oldMod = getSwimSpeedModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.SWIM_SPEED.get()));
					max.removeModifier(oldMod);
				}
			}
		}
	}

	public static void updateSizeModifiers(final Player player) {
		if (DragonStateProvider.getCap(player).isPresent()) {
			DragonStateHandler handler = DragonStateProvider.getHandler(player);
			if (handler.isDragon()) {
				// Grant the dragon attribute modifiers
				double size = handler.getSize();
				AttributeModifier health = buildHealthMod(size);
				updateHealthModifier(player, health);

				AttributeModifier damage = buildDamageMod(handler, handler.isDragon());
				updateDamageModifier(player, damage);

				AttributeModifier reach = buildReachMod(size);
				updateReachModifier(player, reach);

				AttributeModifier stepHeight = buildStepHeightMod(handler, size);
				updateStepHeightModifier(player, stepHeight);

				AttributeModifier moveSpeed = buildMovementSpeedMod(handler, size);
				updateMovementSpeedModifier(player, moveSpeed);
			} else {
				// Remove the dragon attribute modifiers
				AttributeModifier oldMod = getHealthModifier(player);
				if (oldMod != null) {
					float oldMax = player.getMaxHealth();
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH));
					max.removeModifier(oldMod);
					float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
					player.setHealth(newHealth);
				}

				oldMod = getDamageModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE));
					max.removeModifier(oldMod);
				}

				oldMod = getReachModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get()));
					max.removeModifier(oldMod);
				}

				oldMod = getAttackRangeModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.ATTACK_RANGE.get()));
					max.removeModifier(oldMod);
				}

				oldMod = getStepHeightModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()));
					max.removeModifier(oldMod);
				}

				oldMod = getMovementSpeedModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED));
					max.removeModifier(oldMod);
				}

				oldMod = getMovementSpeedModifier(player);
				if (oldMod != null) {
					AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED));
					max.removeModifier(oldMod);
				}
			}
		}
	}

	public static void updateBodyModifiers(Player player) {
		AbstractDragonBody body = DragonUtils.getDragonBody(player);
		boolean isDragon = DragonUtils.isDragon(player);

		AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
		AttributeInstance strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
		AttributeInstance attackKnockbackAttr = player.getAttribute(Attributes.ATTACK_KNOCKBACK);
		AttributeInstance swimSpeedAttr = player.getAttribute(ForgeMod.SWIM_SPEED.get());
		AttributeInstance stepAttr = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
		AttributeInstance gravityAttr = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);

		if (body != null && isDragon) {
			if (speedAttr.getModifier(DRAGON_BODY_MOVEMENT_SPEED) == null || speedAttr.getModifier(DRAGON_BODY_MOVEMENT_SPEED).getAmount() != body.getRunMult()) {
				if (speedAttr.getModifier(DRAGON_BODY_MOVEMENT_SPEED) != null) { speedAttr.removeModifier(DRAGON_BODY_MOVEMENT_SPEED); }
				speedAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_MOVEMENT_SPEED, "BODY_MOVE_SPEED_BONUS", body.getRunMult() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
			}

			if (armorAttr.getModifier(DRAGON_BODY_ARMOR) == null || armorAttr.getModifier(DRAGON_BODY_ARMOR).getAmount() != body.getArmorBonus()) {
				if (armorAttr.getModifier(DRAGON_BODY_ARMOR) != null) { armorAttr.removeModifier(DRAGON_BODY_ARMOR); }
				armorAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_ARMOR, "BODY_ARMOR_BONUS", body.getArmorBonus(), AttributeModifier.Operation.ADDITION));
			}

			if (strengthAttr.getModifier(DRAGON_BODY_STRENGTH) == null || strengthAttr.getModifier(DRAGON_BODY_STRENGTH).getAmount() != body.getDamageBonus()) {
				if (strengthAttr.getModifier(DRAGON_BODY_STRENGTH) != null) { strengthAttr.removeModifier(DRAGON_BODY_STRENGTH); }
				strengthAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_STRENGTH, "BODY_STRENGTH_BONUS", body.getDamageBonus(), AttributeModifier.Operation.ADDITION));
			}

			if (strengthAttr.getModifier(DRAGON_BODY_STRENGTH_MULT) == null || strengthAttr.getModifier(DRAGON_BODY_STRENGTH_MULT).getAmount() != (body.getDamageMult()  - 1)) {
				if (strengthAttr.getModifier(DRAGON_BODY_STRENGTH_MULT) != null) { strengthAttr.removeModifier(DRAGON_BODY_STRENGTH_MULT); }
				strengthAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_STRENGTH_MULT, "BODY_STRENGTH_MULT", body.getDamageMult() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
			}

			if (attackKnockbackAttr.getModifier(DRAGON_BODY_KNOCKBACK_BONUS) == null || attackKnockbackAttr.getModifier(DRAGON_BODY_KNOCKBACK_BONUS).getAmount() != body.getKnockbackBonus()) {
				if (attackKnockbackAttr.getModifier(DRAGON_BODY_KNOCKBACK_BONUS) != null) { attackKnockbackAttr.removeModifier(DRAGON_BODY_KNOCKBACK_BONUS); }
				attackKnockbackAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_KNOCKBACK_BONUS, "BODY_KNOCKBACK_BONUS", body.getKnockbackBonus(), AttributeModifier.Operation.ADDITION));
			}

			if (swimSpeedAttr.getModifier(DRAGON_BODY_SWIM_SPEED_BONUS) == null || swimSpeedAttr.getModifier(DRAGON_BODY_SWIM_SPEED_BONUS).getAmount() != body.getSwimSpeedBonus()) {
				if (swimSpeedAttr.getModifier(DRAGON_BODY_SWIM_SPEED_BONUS) != null) { swimSpeedAttr.removeModifier(DRAGON_BODY_SWIM_SPEED_BONUS); }
				swimSpeedAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_SWIM_SPEED_BONUS, "BODY_SWIM_SPEED_BONUS", body.getSwimSpeedBonus(), AttributeModifier.Operation.ADDITION));
			}

			if (stepAttr.getModifier(DRAGON_BODY_STEP_HEIGHT_BONUS) == null || stepAttr.getModifier(DRAGON_BODY_STEP_HEIGHT_BONUS).getAmount() != body.getStepBonus()) {
				if (stepAttr.getModifier(DRAGON_BODY_STEP_HEIGHT_BONUS) != null) { stepAttr.removeModifier(DRAGON_BODY_STEP_HEIGHT_BONUS); }
				stepAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_STEP_HEIGHT_BONUS, "BODY_STEP_HEIGHT_BONUS", body.getStepBonus(), AttributeModifier.Operation.ADDITION));
			}

			if (gravityAttr.getModifier(DRAGON_BODY_GRAVITY_MULT) == null || gravityAttr.getModifier(DRAGON_BODY_GRAVITY_MULT).getAmount() != (body.getGravityMult() - 1)) {
				if (gravityAttr.getModifier(DRAGON_BODY_GRAVITY_MULT) != null) { gravityAttr.removeModifier(DRAGON_BODY_GRAVITY_MULT); }
				gravityAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_GRAVITY_MULT, "BODY_GRAVITY_MULT", body.getGravityMult() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
			}

			if (healthAttr.getModifier(DRAGON_BODY_HEALTH_MULT) == null || healthAttr.getModifier(DRAGON_BODY_HEALTH_MULT).getAmount() != (body.getHealthMult() - 1)) {
				if (healthAttr.getModifier(DRAGON_BODY_HEALTH_MULT) != null) { healthAttr.removeModifier(DRAGON_BODY_HEALTH_MULT); }
				healthAttr.addTransientModifier(new AttributeModifier(DRAGON_BODY_HEALTH_MULT, "BODY_HEALTH_MULT", body.getHealthMult() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
			}
		} else {
			speedAttr.removeModifier(DRAGON_BODY_MOVEMENT_SPEED);
			armorAttr.removeModifier(DRAGON_BODY_ARMOR);
			strengthAttr.removeModifier(DRAGON_BODY_STRENGTH);
			strengthAttr.removeModifier(DRAGON_BODY_STRENGTH_MULT);
			attackKnockbackAttr.removeModifier(DRAGON_BODY_KNOCKBACK_BONUS);
			swimSpeedAttr.removeModifier(DRAGON_BODY_SWIM_SPEED_BONUS);
			stepAttr.removeModifier(DRAGON_BODY_STEP_HEIGHT_BONUS);
			gravityAttr.removeModifier(DRAGON_BODY_GRAVITY_MULT);
			healthAttr.removeModifier(DRAGON_BODY_HEALTH_MULT);
		}
	}

	@Nullable
	public static AttributeModifier getReachModifier(Player player){
		return Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get())).getModifier(DRAGON_REACH_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getAttackRangeModifier(Player player){
		return Objects.requireNonNull(player.getAttribute(ForgeMod.ATTACK_RANGE.get())).getModifier(DRAGON_REACH_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getHealthModifier(Player player){
		return Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH)).getModifier(DRAGON_HEALTH_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getDamageModifier(Player player){
		return Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getModifier(DRAGON_DAMAGE_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getSwimSpeedModifier(Player player){
		return Objects.requireNonNull(player.getAttribute(ForgeMod.SWIM_SPEED.get())).getModifier(DRAGON_SWIM_SPEED_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getStepHeightModifier(Player player) {
		return Objects.requireNonNull(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get())).getModifier(DRAGON_STEP_HEIGHT_MODIFIER);
	}

	@Nullable
	public static AttributeModifier getMovementSpeedModifier(Player player) {
		return Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED)).getModifier(DRAGON_MOVEMENT_SPEED_MODIFIER);
	}

	public static void updateReachModifier(Player player, AttributeModifier mod){
		if(!ServerConfig.bonuses){
			return;
		}

		AttributeInstance entityReach = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get()));
		entityReach.removeModifier(mod);
		entityReach.addPermanentModifier(mod);

		AttributeInstance attackRange = Objects.requireNonNull(player.getAttribute(ForgeMod.ATTACK_RANGE.get()));
		attackRange.removeModifier(mod);
		attackRange.addPermanentModifier(mod);
	}

	public static void updateHealthModifier(Player player, AttributeModifier mod){
		if(!ServerConfig.healthAdjustments){
			return;
		}
		float oldMax = player.getMaxHealth();
		AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH));
		max.removeModifier(mod);
		max.addPermanentModifier(mod);
		float newHealth = player.getHealth() * player.getMaxHealth() / oldMax;
		player.setHealth(newHealth);
	}

	public static void updateDamageModifier(Player player, AttributeModifier mod){
		if(!ServerConfig.bonuses || !ServerConfig.attackDamage){
			return;
		}
		AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE));
		max.removeModifier(mod);
		max.addPermanentModifier(mod);
	}

	public static void updateSwimSpeedModifier(Player player, AttributeModifier mod){
		if(!ServerConfig.bonuses || !ServerConfig.seaSwimmingBonuses){
			return;
		}
		AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.SWIM_SPEED.get()));
		max.removeModifier(mod);
		max.addPermanentModifier(mod);
	}

	public static void updateStepHeightModifier(Player player, AttributeModifier mod) {
		if(!ServerConfig.bonuses) {
			return;
		}
		AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()));
		max.removeModifier(mod);
		max.addPermanentModifier(mod);
	}

	public static void updateMovementSpeedModifier(Player player, AttributeModifier mod) {
		if(!ServerConfig.bonuses) {
			return;
		}
		AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MOVEMENT_SPEED));
		max.removeModifier(mod);
		max.addPermanentModifier(mod);
	}

	public static double getJumpBonus(DragonStateHandler handler) {
		double jumpBonus = 0;
		if (handler.getBody() != null) {
			jumpBonus = handler.getBody().getJumpBonus();
			if (ServerConfig.allowLargeScaling && handler.getSize() > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
				jumpBonus += ServerConfig.largeJumpHeightScalar * (handler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
			}
		}
		switch(handler.getLevel()){
			case NEWBORN -> jumpBonus += ServerConfig.newbornJump; //1+ block
			case YOUNG -> jumpBonus += ServerConfig.youngJump; //1.5+ block
			case ADULT -> jumpBonus += ServerConfig.adultJump; //2+ blocks
		}

		return jumpBonus;
	}
}