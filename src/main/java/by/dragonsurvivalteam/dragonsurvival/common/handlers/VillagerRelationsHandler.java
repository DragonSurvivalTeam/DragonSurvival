package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.VillageRelationShips;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.DragonHunter;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class VillagerRelationsHandler{
	public static List<Supplier<EntityType<? extends PathfinderMob>>> dragonHunters;

	private static int timeLeft = Functions.minutesToTicks(ServerConfig.royalSpawnDelay) + Functions.minutesToTicks(ThreadLocalRandom.current().nextInt(30));

	/// FIXME: It looks like we are modifying the drops of certain entities here. We shouldn't be doing this in an event like this! Use global loot modifiers or a custom loot table!
	@SubscribeEvent
	public static void onDeath(LivingDeathEvent deathEvent){
		LivingEntity livingEntity = deathEvent.getEntity();
		Entity killer = deathEvent.getSource().getEntity();
		if(killer instanceof Player playerEntity){
			if(livingEntity instanceof AbstractVillager){
				Level world = killer.level();
					if(DragonStateProvider.isDragon(killer)){
						AbstractVillager villagerEntity = (AbstractVillager)livingEntity;

						MerchantOffers merchantOffers = villagerEntity.getOffers();

						if(villagerEntity instanceof Villager villager){
							int level = villager.getVillagerData().getLevel();

							if(world.random.nextInt(100) < 30){
								Optional<MerchantOffer> offer = merchantOffers.stream().filter(merchantOffer -> merchantOffer.getResult().getItem() != Items.EMERALD).toList().stream().findAny();

								offer.ifPresent(merchantOffer -> world.addFreshEntity(new ItemEntity(world, villager.getX(), villager.getY(), villager.getZ(), merchantOffer.getResult())));
							}

							if(!world.isClientSide()){
								playerEntity.giveExperiencePoints(level * ServerConfig.xpGain);
								//                                applyEvilMarker(playerEntity);
							}
						}else if(villagerEntity instanceof WanderingTrader wanderingTrader){
							if(!world.isClientSide()){
								playerEntity.giveExperiencePoints(2 * ServerConfig.xpGain);
								if(world.random.nextInt(100) < 30){
									ItemStack itemStack = wanderingTrader.getOffers().stream().filter(merchantOffer -> merchantOffer.getResult().getItem() != Items.EMERALD).toList().get(wanderingTrader.getRandom().nextInt(wanderingTrader.getOffers().size())).getResult();
									world.addFreshEntity(new ItemEntity(world, wanderingTrader.getX(), wanderingTrader.getY(), wanderingTrader.getZ(), itemStack));
								}
								//                                applyEvilMarker(playerEntity);
							}
						}
					}
			}

			String typeName = ResourceHelper.getKey(livingEntity).toString();
			// FIXME: royalChaseStatusGivers should get parsed in DragonConfigHandler and turned into a HashSet<Entity> or something similar
			if(DragonStateProvider.isDragon(playerEntity) && ServerConfig.royalChaseStatusGivers.contains(typeName)){
				applyEvilMarker(playerEntity);
			}
		}
	}

	public static void applyEvilMarker(Player playerEntity){
		DragonStateProvider.getCap(playerEntity).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				MobEffectInstance effectInstance = playerEntity.getEffect(DSEffects.ROYAL_CHASE);
				if(effectInstance == null){
					playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(1)));
				}else{
					int duration = effectInstance.getDuration();
					if(duration <= Functions.minutesToTicks(1)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(5), 1));
					}else if(duration <= Functions.minutesToTicks(5)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(10), 2));
					}else if(duration <= Functions.minutesToTicks(10)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(15), 3));
					}else if(duration <= Functions.minutesToTicks(15)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(20), 4));
					}else if(duration <= Functions.minutesToTicks(20)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(25), 5));
					}else if(duration <= Functions.minutesToTicks(25)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(30), 6));
					}else if(duration <= Functions.minutesToTicks(30)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(60), 7));
					}else if(duration <= Functions.minutesToTicks(60)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(120), 8));
					}else if(duration <= Functions.minutesToTicks(120)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(240), 9));
					}else if(duration <= Functions.minutesToTicks(240)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(400), 10));
					}else if(duration <= Functions.minutesToTicks(400)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(600), 11));
					}else if(duration <= Functions.minutesToTicks(600)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(800), 12));
					}else if(duration <= Functions.minutesToTicks(800)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(1000), 13));
					}else if(duration <= Functions.minutesToTicks(1000)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(1200), 14));
					}else if(duration <= Functions.minutesToTicks(1200)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(1500), 15));
					}else if(duration <= Functions.minutesToTicks(1500)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(2000), 16));
					}else if(duration <= Functions.minutesToTicks(2000)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(3000), 17));
					}else if(duration <= Functions.minutesToTicks(3000)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(4000), 18));
					}else if(duration <= Functions.minutesToTicks(4000)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(5000), 19));
					}else if(duration <= Functions.minutesToTicks(5000)){
						playerEntity.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.minutesToTicks(6000), 20));
					}
				}
			}
		});
	}


	@SubscribeEvent
	public static void voidEvilStatus(MobEffectEvent.Added potionAddedEvent){
		MobEffectInstance effectInstance = potionAddedEvent.getEffectInstance();
		LivingEntity livingEntity = potionAddedEvent.getEntity();
		if(effectInstance.getEffect() == MobEffects.HERO_OF_THE_VILLAGE){
			livingEntity.removeEffect(DSEffects.ROYAL_CHASE);
		}
	}

	@SubscribeEvent
	public static void specialTasks(EntityJoinLevelEvent joinWorldEvent){
		Level world = joinWorldEvent.getLevel();
		Entity entity = joinWorldEvent.getEntity();
		if(entity instanceof IronGolem golemEntity){
			golemEntity.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(golemEntity, Player.class, 0, true, false, livingEntity -> livingEntity.hasEffect(DSEffects.ROYAL_CHASE)));
		}

		if(entity instanceof AbstractVillager abstractVillager){
			abstractVillager.goalSelector.addGoal(10, new AvoidEntityGoal<>(abstractVillager, Player.class, livingEntity -> livingEntity.hasEffect(DSEffects.ROYAL_CHASE), 16.0F, 1.0D, 1.0D, pMob -> true));
		}
	}

	@SubscribeEvent
	public static void interactions(PlayerInteractEvent.EntityInteract event){
		Player playerEntity = event.getEntity();
		Entity livingEntity = event.getTarget();
		if(livingEntity instanceof AbstractVillager){
			if(playerEntity.hasEffect(DSEffects.ROYAL_CHASE)){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void hurtEntity(LivingIncomingDamageEvent attackEntityEvent){
		Entity attacked = attackEntityEvent.getEntity();
		Player attacker = attackEntityEvent.getSource().getEntity() instanceof Player ? (Player)attackEntityEvent.getSource().getEntity() : null;

		if(attacker == null){
			return;
		}

		if(attacked instanceof AbstractVillager || attacked instanceof DragonHunter){
			{
				if(attacker.hasEffect(DSEffects.ROYAL_CHASE)){
					int duration = attacker.getEffect(DSEffects.ROYAL_CHASE).getDuration();
					int amplifier = attacker.getEffect(DSEffects.ROYAL_CHASE).getAmplifier();
					attacker.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, duration + Functions.secondsToTicks(5), amplifier));
				}else{
					attacker.addEffect(new MobEffectInstance(DSEffects.ROYAL_CHASE, Functions.secondsToTicks(5)));
				}
			}
		}
	}

	@SubscribeEvent
	public static void spawnHunters(PlayerTickEvent.Post playerTickEvent){
		if(!dragonHunters.isEmpty()){
			Player player = playerTickEvent.getEntity();
			if(player.level() instanceof ServerLevel serverLevel && !player.isCreative() && !player.isSpectator() && player.isAlive() && player.hasEffect(DSEffects.ROYAL_CHASE) && DragonStateProvider.isDragon(player)){
				if(serverLevel.dimension() == Level.OVERWORLD){
					VillageRelationShips villageRelationShips = DragonStateProvider.getOrGenerateHandler(player).getVillageRelationShips();
						if(villageRelationShips.hunterSpawnDelay == 0){
							BlockPos spawnPosition = SpawningUtils.findRandomSpawnPosition(player, 1, 4, 14.0F);
							if(spawnPosition != null && spawnPosition.getY() >= ServerConfig.riderSpawnLowerBound && spawnPosition.getY() <= ServerConfig.riderSpawnUpperBound){
								if (serverLevel.getBiome(spawnPosition).is(Tags.Biomes.IS_AQUATIC)) {
									return;
								}
								int levelOfEvil = computeLevelOfEvil(player);
								for(int i = 0; i < levelOfEvil; i++){
									SpawningUtils.spawn(Objects.requireNonNull(dragonHunters.get(serverLevel.random.nextInt(dragonHunters.size())).get().create(serverLevel)), spawnPosition, serverLevel);
								}
								if(serverLevel.isCloseToVillage(player.blockPosition(), 3)){
									villageRelationShips.hunterSpawnDelay = Functions.minutesToTicks(ServerConfig.hunterSpawnDelay / 3) + Functions.minutesToTicks(serverLevel.random.nextInt(ServerConfig.hunterSpawnDelay / 6));
								}else{
									villageRelationShips.hunterSpawnDelay = Functions.minutesToTicks(ServerConfig.hunterSpawnDelay) + Functions.minutesToTicks(serverLevel.random.nextInt(ServerConfig.hunterSpawnDelay / 3));
								}
							}
						}else{
							villageRelationShips.hunterSpawnDelay--;
						}
				}
			}
		}
	}

	public static int computeLevelOfEvil(Player playerEntity){
		if(DragonStateProvider.isDragon(playerEntity) && playerEntity.hasEffect(DSEffects.ROYAL_CHASE)){
			MobEffectInstance effectInstance = playerEntity.getEffect(DSEffects.ROYAL_CHASE);
			assert effectInstance != null;
			int timeLeft = effectInstance.getDuration();
			if(timeLeft >= Functions.minutesToTicks(5000)){
				return 20;
			}
			if(timeLeft >= Functions.minutesToTicks(4000)){
				return 19;
			}
			if(timeLeft >= Functions.minutesToTicks(3000)){
				return 18;
			}
			if(timeLeft >= Functions.minutesToTicks(2000)){
				return 17;
			}
			if(timeLeft >= Functions.minutesToTicks(1500)){
				return 16;
			}
			if(timeLeft >= Functions.minutesToTicks(1200)){
				return 15;
			}
			if(timeLeft >= Functions.minutesToTicks(1000)){
				return 14;
			}
			if(timeLeft >= Functions.minutesToTicks(800)){
				return 13;
			}
			if(timeLeft >= Functions.minutesToTicks(600)){
				return 12;
			}
			if(timeLeft >= Functions.minutesToTicks(400)){
				return 11;
			}
			if(timeLeft >= Functions.minutesToTicks(240)){
				return 10;
			}
			if(timeLeft >= Functions.minutesToTicks(120)){
				return 9;
			}
			if(timeLeft >= Functions.minutesToTicks(60)){
				return 8;
			}
			if(timeLeft >= Functions.minutesToTicks(30)){
				return 7;
			}
			if(timeLeft >= Functions.minutesToTicks(25)){
				return 6;
			}
			if(timeLeft >= Functions.minutesToTicks(20)){
				return 5;
			}
			if(timeLeft >= Functions.minutesToTicks(15)){
				return 4;
			}
			if(timeLeft >= Functions.minutesToTicks(10)){
				return 3;
			}
			if(timeLeft >= Functions.minutesToTicks(5)){
				return 2;
			}
			if(timeLeft >= Functions.minutesToTicks(1)){
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Save duration of 'evil dragon'
	 */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post playerTickEvent){
			Player playerEntity = playerTickEvent.getEntity();
			if(!playerEntity.level().isClientSide()){
				if(playerEntity.hasEffect(DSEffects.ROYAL_CHASE)){
					DragonStateProvider.getOrGenerateHandler(playerEntity).getVillageRelationShips().evilStatusDuration = playerEntity.getEffect(DSEffects.ROYAL_CHASE).getDuration();
				}
			}
	}
}