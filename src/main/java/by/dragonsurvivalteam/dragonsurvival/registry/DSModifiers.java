package by.dragonsurvivalteam.dragonsurvival.registry;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public class DSModifiers {

	public record ModifierBuilder(ResourceLocation modifier, Holder<Attribute> attribute, Operation operation, Function<DragonStateHandler, Double> calculator) {
		private AttributeModifier buildModifier(DragonStateHandler handler) {
			return new AttributeModifier(modifier, calculator.apply(handler), operation);
		}

		public void updateModifier(Player player) {
			// Special case for health modifier
			float oldMax = player.getMaxHealth();

			DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);
			AttributeInstance instance = player.getAttribute(attribute);
			if (instance == null) { return; }
			AttributeModifier oldMod = instance.getModifier(modifier);
			if (oldMod != null) {
				instance.removeModifier(oldMod);
			}

			if(handler.isDragon()) {
				AttributeModifier builtModifier = buildModifier(handler);
				instance.addPermanentModifier(builtModifier);
				if(attribute == Attributes.MAX_HEALTH) {
					float newHealth = Math.min(player.getMaxHealth(), player.getHealth() * player.getMaxHealth() / oldMax);
					player.setHealth(newHealth);
				}
			}
		}
	}

	public static final ResourceLocation DRAGON_REACH_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_reach_modifier");
	public static final ResourceLocation DRAGON_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_health_modifier");
	public static final ResourceLocation DRAGON_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_damage_modifier");
	public static final ResourceLocation DRAGON_SWIM_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_swim_speed_modifier");
	public static final ResourceLocation DRAGON_STEP_HEIGHT_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_step_height_modifier");
	public static final ResourceLocation DRAGON_MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_movement_speed_modifier");
	public static final ResourceLocation DRAGON_JUMP_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_jump_bonus");
	public static final ResourceLocation DRAGON_SAFE_FALL_DISTANCE = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_safe_fall_distance");
	public static final ResourceLocation DRAGON_SUBMERGED_MINING_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_submerged_mining_speed");

	public static final ResourceLocation DRAGON_BODY_MOVEMENT_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_movement_speed");
	public static final ResourceLocation DRAGON_BODY_HEALTH_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_health_bonus");
	public static final ResourceLocation DRAGON_BODY_ARMOR = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_armor");
	public static final ResourceLocation DRAGON_BODY_STRENGTH = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_strength");
	public static final ResourceLocation DRAGON_BODY_STRENGTH_MULT = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_strength_mult");
	public static final ResourceLocation DRAGON_BODY_KNOCKBACK_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_knockback_bonus");
	public static final ResourceLocation DRAGON_BODY_SWIM_SPEED_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_swim_speed_bonus");
	public static final ResourceLocation DRAGON_BODY_STEP_HEIGHT_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_step_height_bonus");
	public static final ResourceLocation DRAGON_BODY_GRAVITY_MULT = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_gravity_mult");
	public static final ResourceLocation DRAGON_BODY_HEALTH_MULT = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_health_mult");
	public static final ResourceLocation DRAGON_BODY_JUMP_BONUS = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_jump_bonus");
	public static final ResourceLocation DRAGON_BODY_SAFE_FALL_DISTANCE = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_safe_fall_distance");
	public static final ResourceLocation DRAGON_BODY_FLIGHT_STAMINA = ResourceLocation.fromNamespaceAndPath(MODID, "dragon_body_flight_stamina");

	// Used in MixinPlayerEntity to add the slow falling effect to dragons
	public static final ResourceLocation SLOW_FALLING = ResourceLocation.fromNamespaceAndPath(MODID, "slow_falling");

	// Used in EmoteHandler to keep track of the no move state
	public static final ResourceLocation EMOTE_NO_MOVE = ResourceLocation.fromNamespaceAndPath(MODID, "emote_no_move");

	// Modifier from the bolas item
	public static final ResourceLocation SLOW_MOVEMENT = ResourceLocation.fromNamespaceAndPath(MODID, "slow_movement");

	// Modifier for tough skin ability
	public static final ResourceLocation TOUGH_SKIN = ResourceLocation.fromNamespaceAndPath(MODID, "tough_skin");

	public static final List<ModifierBuilder> TYPE_MODIFIER_BUILDERS = List.of(
			new ModifierBuilder(DRAGON_SWIM_SPEED_MODIFIER, NeoForgeMod.SWIM_SPEED, Operation.ADD_VALUE, DSModifiers::buildSwimSpeedMod),
			new ModifierBuilder(DRAGON_SUBMERGED_MINING_SPEED, Attributes.SUBMERGED_MINING_SPEED, Operation.ADD_MULTIPLIED_TOTAL, DSModifiers::buildSubmergedMiningSpeedMod)
	);

	public static final List<ModifierBuilder> SIZE_MODIFIER_BUILDERS = List.of(
			new ModifierBuilder(DRAGON_HEALTH_MODIFIER, Attributes.MAX_HEALTH, Operation.ADD_VALUE, DSModifiers::buildHealthMod),
			new ModifierBuilder(DRAGON_DAMAGE_MODIFIER, Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, DSModifiers::buildDamageMod),
			new ModifierBuilder(DRAGON_REACH_MODIFIER, Attributes.BLOCK_INTERACTION_RANGE, Operation.ADD_MULTIPLIED_BASE, DSModifiers::buildReachMod),
			new ModifierBuilder(DRAGON_STEP_HEIGHT_MODIFIER, Attributes.STEP_HEIGHT, Operation.ADD_VALUE, DSModifiers::buildStepHeightMod),
			new ModifierBuilder(DRAGON_MOVEMENT_SPEED_MODIFIER, Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, DSModifiers::buildMovementSpeedMod),
			new ModifierBuilder(DRAGON_JUMP_BONUS, Attributes.JUMP_STRENGTH, Operation.ADD_VALUE, DSModifiers::buildJumpMod),
			new ModifierBuilder(DRAGON_SAFE_FALL_DISTANCE, Attributes.SAFE_FALL_DISTANCE, Operation.ADD_VALUE, DSModifiers::buildJumpMod)
	);

	public static final List<ModifierBuilder> BODY_MODIFIER_BUILDERS = List.of(
			new ModifierBuilder(DRAGON_BODY_MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, handler -> handler.getBody().getRunMult() - 1),
			new ModifierBuilder(DRAGON_BODY_ARMOR, Attributes.ARMOR, Operation.ADD_VALUE, handler -> handler.getBody().getArmorBonus()),
			new ModifierBuilder(DRAGON_BODY_STRENGTH, Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, handler -> handler.getBody().getDamageBonus()),
			new ModifierBuilder(DRAGON_BODY_STRENGTH_MULT, Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, handler -> handler.getBody().getDamageMult() - 1),
			new ModifierBuilder(DRAGON_BODY_KNOCKBACK_BONUS, Attributes.ATTACK_KNOCKBACK, Operation.ADD_VALUE, handler -> handler.getBody().getKnockbackBonus()),
			new ModifierBuilder(DRAGON_BODY_SWIM_SPEED_BONUS, NeoForgeMod.SWIM_SPEED, Operation.ADD_VALUE, handler -> handler.getBody().getSwimSpeedBonus()),
			new ModifierBuilder(DRAGON_BODY_STEP_HEIGHT_BONUS, Attributes.STEP_HEIGHT, Operation.ADD_VALUE, handler -> handler.getBody().getStepBonus()),
			new ModifierBuilder(DRAGON_BODY_GRAVITY_MULT, Attributes.GRAVITY, Operation.ADD_MULTIPLIED_TOTAL, handler -> handler.getBody().getGravityMult() - 1),
			new ModifierBuilder(DRAGON_BODY_HEALTH_BONUS, Attributes.MAX_HEALTH, Operation.ADD_VALUE, handler -> handler.getBody().getHealthBonus()),
			new ModifierBuilder(DRAGON_BODY_HEALTH_MULT, Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, handler -> handler.getBody().getHealthMult() - 1),
			new ModifierBuilder(DRAGON_BODY_JUMP_BONUS, Attributes.JUMP_STRENGTH, Operation.ADD_VALUE, handler -> handler.getBody().getJumpBonus()),
			new ModifierBuilder(DRAGON_BODY_SAFE_FALL_DISTANCE, Attributes.SAFE_FALL_DISTANCE, Operation.ADD_VALUE, handler -> handler.getBody().getJumpBonus()),
			new ModifierBuilder(DRAGON_BODY_FLIGHT_STAMINA, DSAttributes.FLIGHT_STAMINA_COST, Operation.ADD_MULTIPLIED_TOTAL, handler -> handler.getBody().getFlightStaminaMult())
	);

	public static double buildHealthMod(DragonStateHandler handler){
		if (!ServerConfig.healthAdjustments) return 0;
		double healthModifier;
		double size = handler.getSize();
		if(ServerConfig.allowLargeScaling && size > ServerConfig.maxHealthSize) {
			double healthModifierPercentage = Math.min(1.0, (size - ServerConfig.maxHealthSize) / (ServerConfig.maxGrowthSize - DragonLevel.ADULT.size));
			healthModifier = Mth.lerp(healthModifierPercentage, ServerConfig.maxHealth, ServerConfig.largeMaxHealth) - 20;
		}
		else {
			double healthModifierPercentage = Math.min(1.0, (size - DragonLevel.NEWBORN.size) / (ServerConfig.maxHealthSize - DragonLevel.NEWBORN.size));
			healthModifier = Mth.lerp(healthModifierPercentage, ServerConfig.minHealth, ServerConfig.maxHealth) - 20;
		}
		return healthModifier;
	}

	public static double buildReachMod(DragonStateHandler handler){
		double reachModifier;
		double size = handler.getSize();
		if(ServerConfig.allowLargeScaling && size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
			reachModifier = ServerConfig.reachBonus + ServerConfig.largeReachScalar * (size / ServerConfig.DEFAULT_MAX_GROWTH_SIZE);
		}
		else {
			reachModifier = Math.max(ServerConfig.reachBonus, (size - DragonLevel.NEWBORN.size) / (ServerConfig.DEFAULT_MAX_GROWTH_SIZE - DragonLevel.NEWBORN.size) * ServerConfig.reachBonus);
		}
		return reachModifier;
	}

	public static double buildDamageMod(DragonStateHandler handler) {
		if (!ServerConfig.attackDamage) return 0;
		double ageBonus = handler.getLevel() == DragonLevel.ADULT ? ServerConfig.adultBonusDamage : handler.getLevel() == DragonLevel.YOUNG ? ServerConfig.youngBonusDamage : ServerConfig.babyBonusDamage;
		if(ServerConfig.allowLargeScaling && handler.getSize() > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
			double damageModPercentage = Math.min(1.0, (handler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / (ServerConfig.maxGrowthSize - ServerConfig.DEFAULT_MAX_GROWTH_SIZE));
			ageBonus = Mth.lerp(damageModPercentage, ageBonus, ServerConfig.largeDamageBonus);
		}
		return ageBonus;
	}

	public static double buildSwimSpeedMod(DragonStateHandler handler){
		return Objects.equals(handler.getType(), DragonTypes.SEA) && ServerConfig.seaSwimmingBonuses ? 1 : 0;
	}

	public static double buildStepHeightMod(DragonStateHandler handler) {
		double size = handler.getSize();
		double stepHeightBonus = handler.getLevel() == DragonLevel.ADULT ? ServerConfig.adultStepHeight : handler.getLevel() == DragonLevel.YOUNG ? ServerConfig.youngStepHeight : ServerConfig.newbornStepHeight;
		if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE && ServerConfig.allowLargeScaling)  {
			stepHeightBonus += ServerConfig.largeStepHeightScalar * (size - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
		}
		return stepHeightBonus;
	}

	public static double buildMovementSpeedMod(DragonStateHandler handler) {
		double moveSpeedMultiplier = 1;
		double size = handler.getSize();
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
		return moveSpeedMultiplier - 1;
	}

	private static double buildJumpMod(DragonStateHandler handler) {
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

	public static double buildSubmergedMiningSpeedMod(DragonStateHandler handler) {
		return Objects.equals(handler.getType(), DragonTypes.SEA) ? 4.0 : 0.0;
	}

	public static void updateAllModifiers(Player player) {
		updateTypeModifiers(player);
		updateSizeModifiers(player);
		updateBodyModifiers(player);
	}

	public static void updateTypeModifiers(Player player) {
		for(ModifierBuilder builder : TYPE_MODIFIER_BUILDERS) {
			builder.updateModifier(player);
		}
	}

	public static void updateSizeModifiers(Player player) {
		for(ModifierBuilder builder : SIZE_MODIFIER_BUILDERS) {
			builder.updateModifier(player);
		}
	}

	public static void updateBodyModifiers(Player player) {
		for(ModifierBuilder builder : BODY_MODIFIER_BUILDERS) {
			builder.updateModifier(player);
		}
	}
}