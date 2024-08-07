package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaSweepParticle;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active.HunterAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.RevealingTheSoulAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SpectralImpactAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.DataBlockTagProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.DataDamageTypeTagsProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler.*;
import static by.dragonsurvivalteam.dragonsurvival.util.DragonLevel.*;
import static by.dragonsurvivalteam.dragonsurvival.util.DragonLevel.ADULT;

@SuppressWarnings("unused")
@EventBusSubscriber
public class MagicHandler{
	@SubscribeEvent
	public static void magicUpdate(PlayerTickEvent.Post event){
		Player player = event.getEntity();

		AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(cap.isDragon()) {
				if(cap.getMagicData().abilities.isEmpty() || cap.getMagicData().innateDragonAbilities.isEmpty() || cap.getMagicData().activeDragonAbilities.isEmpty()){
					cap.getMagicData().initAbilities(cap.getType());
				}
	
				for(int i = 0; i < MagicCap.activeAbilitySlots; i++){
					ActiveDragonAbility ability = cap.getMagicData().getAbilityFromSlot(i);
	
					if(ability != null){
						ability.tickCooldown();
					}
				}
			}
		});
	}

	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event){
		Player player = event.getEntity();

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon()){
				return;
			}

			for(DragonAbility ability : cap.getMagicData().abilities.values()){
				ability.player = player;
			}

			if (player.hasEffect(DSEffects.HUNTER)) {
				BlockState blockStateFeet = player.getBlockStateOn();

				if (isHunterRelevant(blockStateFeet)) {
					player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false));
				}

				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 2, false, false));
			}
		});
	}

	private static boolean isHunterRelevant(final BlockState blockState) {
		return blockState.is(DataBlockTagProvider.HUNTER_ABILITY_BLOCKS);
	}

	@SubscribeEvent
	public static void livingVisibility(LivingEvent.LivingVisibilityEvent event){
		if(event.getEntity() instanceof Player player){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(player.hasEffect(DSEffects.HUNTER)){
					event.modifyVisibility(0);
				}
			});
		}
	}

	@SubscribeEvent
	public static void livingTick(EntityTickEvent.Post event){
		if(event.getEntity() instanceof LivingEntity entity) {
			EntityStateHandler cap = EntityStateProvider.getEntityHandler(entity);

			if(entity.hasEffect(DSEffects.BURN)){
				if(entity.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || entity.isInWaterRainOrBubble()){
					entity.removeEffect(DSEffects.BURN);
				}
			}

			if(entity.tickCount % 20 == 0){
				MobEffectInstance drainEffect = entity.getEffect(DSEffects.DRAIN);

				if (drainEffect != null) {
					if (!DragonUtils.isDragonType(entity, DragonTypes.FOREST)) {
						Player player = cap.lastAfflicted != -1 && entity.level().getEntity(cap.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(cap.lastAfflicted) : null;

						if (player != null) {
							TargetingFunctions.attackTargets(player, ent -> ent.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.FOREST_DRAGON_DRAIN), player), drainEffect.getAmplifier() + 1), entity);
						} else {
							entity.hurt(entity.damageSources().magic(), drainEffect.getAmplifier() + 1);
						}
					}
				}

				MobEffectInstance chargedEffect = entity.getEffect(DSEffects.CHARGED);

				if (chargedEffect != null) {
					Player player = cap.lastAfflicted != -1 && entity.level().getEntity(cap.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(cap.lastAfflicted) : null;
					if (!DragonUtils.isDragonType(entity, DragonTypes.SEA)) {
						StormBreathAbility.chargedEffectSparkle(player, entity, StormBreathAbility.chargedChainRange, StormBreathAbility.chargedEffectChainCount, (chargedEffect.getAmplifier() + 1) * StormBreathAbility.chargedEffectDamageMultiplier);
					}
				}

				MobEffectInstance burnEffect = entity.getEffect(DSEffects.BURN);

				if (burnEffect != null) {
					if (!entity.fireImmune()) {
						if (cap.lastPos != null) {
							double distance = entity.distanceToSqr(cap.lastPos);
							float damage = (burnEffect.getAmplifier() + 1) * Mth.clamp((float) distance, 0, 10);

							if (damage > 0) {
								if (!entity.isOnFire()) {
									// Short enough fire duration to not cause fire damage but still drop cooked items
									entity.setRemainingFireTicks(1);
								}
								Player player = cap.lastAfflicted != -1 && entity.level().getEntity(cap.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(cap.lastAfflicted) : null;
								if (player != null) {
									TargetingFunctions.attackTargets(player, ent -> ent.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.CAVE_DRAGON_BURN), player), damage), entity);
								} else {
									entity.hurt(entity.damageSources().onFire(), damage);
								}
							}
						}
					}
				}

				cap.lastPos = entity.position();
			}
		}
	}

	@SubscribeEvent
	public static void playerStruckByLightning(EntityStruckByLightningEvent event){
		if(event.getEntity() instanceof Player player){
			
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(Objects.equals(cap.getType(), DragonTypes.SEA)){
					event.setCanceled(true);
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerDamaged(LivingIncomingDamageEvent event){
		if(event.getEntity() instanceof Player player){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}

				if(player.hasEffect(DSEffects.HUNTER)){
					player.removeEffect(DSEffects.HUNTER);
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerHitEntity(CriticalHitEvent event){
		Player player = event.getEntity();
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon()){
				return;
			}

			if(player.hasEffect(DSEffects.HUNTER)){
				MobEffectInstance hunter = player.getEffect(DSEffects.HUNTER);
				player.removeEffect(DSEffects.HUNTER);
				event.setDamageMultiplier(event.getDamageMultiplier() + (float)((hunter.getAmplifier() + 1) * HunterAbility.hunterDamageBonus));
			}
		});
	}

	public static void applyDebuffs(final MobEffectEvent.Added event) {
		if (event.getEffectInstance() == null || Objects.equals(event.getEffectSource(), event.getEntity())) return;

		MobEffectInstance effect = event.getEffectInstance();
		int amp = effect.getAmplifier();

		if (event.getEffectInstance().getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)) {
			if (event.getEffectSource() instanceof LivingEntity source) {
				Holder<Enchantment> holder = EnchantmentUtils.getHolder(DSEnchantments.OVERWHELMING_MIGHT);
				if (holder != null)
					amp += EnchantmentHelper.getEnchantmentLevel(holder, source);
			}

			Holder<Enchantment> holder = EnchantmentUtils.getHolder(DSEnchantments.UNBREAKABLE_SPIRIT);
			if (holder != null)
				amp -= EnchantmentHelper.getEnchantmentLevel(holder, event.getEntity());

			amp = Math.min(Math.max(amp, 0), 255);

			if (amp != effect.getAmplifier()) {
				MobEffectInstance newInstance = new MobEffectInstance(effect.getEffect(), effect.getDuration(), amp);
				event.getEntity().removeEffect(effect.getEffect());
				event.getEntity().addEffect(newInstance);
			}
		}
	}

	@SubscribeEvent
	public static void livingHurt(final LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof LivingEntity entity) {
			if (event.getSource().getEntity() instanceof LivingEntity source) {
				if (entity.hasEffect(DSEffects.BLOOD_SIPHON)) {
					source.heal(event.getAmount() * 0.1f);
				}
				if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.registryAccess().registry(Registries.ENCHANTMENT).isPresent()) {
					Registry<Enchantment> enchantments = Minecraft.getInstance().level.registryAccess().registry(Registries.ENCHANTMENT).get();
					if (event.getSource().is(DataDamageTypeTagsProvider.DRAGON_MAGIC)) {
						Optional<Holder.Reference<Enchantment>> draconicSuperiority = enchantments.getHolder(DSEnchantments.DRACONIC_SUPERIORITY);
						if (draconicSuperiority.isPresent()) {
							EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), source);
							event.setAmount(event.getAmount() * 1.2f + (0.08f * EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), source)));
						}
					}
					if (event.getEntity().getHealth() == event.getEntity().getMaxHealth()) {
						Optional<Holder.Reference<Enchantment>> murderersCunning = enchantments.getHolder(DSEnchantments.MURDERERS_CUNNING);
						murderersCunning.ifPresent(enchantmentReference -> event.setAmount(event.getAmount() * 1.4f + (0.2f * EnchantmentHelper.getEnchantmentLevel(enchantmentReference, source))));
					}
				}
			}
		}

		if (event.getSource().is(DataDamageTypeTagsProvider.DRAGON_BREATH)) {
			return;
		}

		if (event.getSource().getEntity() instanceof Player player) {
			DragonStateProvider.getCap(player).ifPresent(handler -> {
				if (!handler.isDragon()) {
					return;
				}

				if (Objects.equals(handler.getType(), DragonTypes.SEA)) {
					SpectralImpactAbility spectralImpact = DragonAbilities.getSelfAbility(player, SpectralImpactAbility.class);
					boolean hit = player.getRandom().nextInt(100) <= spectralImpact.getChance(); // TODO Check :: Can the next int be 0? In that case the effect would trigger

					if (hit) {
						event.getEntity().hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.SPECTRAL_IMPACT), player), (float) (event.getAmount() * 0.15));
						double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
						double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));

						if (player.level() instanceof ServerLevel serverLevel) {
							serverLevel.sendParticles(new SeaSweepParticle.Data(0), player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
						}
					}
				} else if (Objects.equals(handler.getType(), DragonTypes.CAVE)) {
					BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);
					boolean hit = player.getRandom().nextInt(100) < burnAbility.getChance();

					if (hit) {
						EntityStateProvider.getEntityHandler(event.getEntity()).lastAfflicted = player.getId();

                        if (!player.level().isClientSide()) {
							event.getEntity().addEffect(new MobEffectInstance(DSEffects.BURN, Functions.secondsToTicks(30)));
						}
					}
				}
			});
		}
	}

	@SubscribeEvent
	public static void experienceDrop(LivingExperienceDropEvent event){
		Player player = event.getAttackingPlayer();

		if(player != null){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(!cap.isDragon()){
					return;
				}
				
				double expMult = 1.0;
				AbstractDragonBody body = DragonUtils.getDragonBody(player);
				if (body != null) {
					expMult = body.getExpMult();
				}

				if(player.hasEffect(DSEffects.REVEALING_THE_SOUL)){
					int extra = (int)Math.min(RevealingTheSoulAbility.revealingTheSoulMaxEXP, event.getDroppedExperience() * RevealingTheSoulAbility.revealingTheSoulMultiplier);
					event.setDroppedExperience((int) ((event.getDroppedExperience() + extra) * expMult));
				}
			});
		}
	}
}