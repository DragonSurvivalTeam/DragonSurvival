package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Used in pair with {@link ClientFlightHandler}
 */
@EventBusSubscriber()
@SuppressWarnings( "unused" )
public class ServerFlightHandler{

	public static final int spinDuration = (int)Math.round(0.76 * 20);

	@ConfigRange( min = 0.1, max = 1 )
	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "maxFlightSpeed", comment = "Maximum acceleration fly speed up and down. Take into account the chunk load speed. A speed of 0.3 is optimal." )
	public static Double maxFlightSpeed = 0.3;

	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "startWithLevitation", comment = "Whether dragons can use levitation magic from birth." )
	public static Boolean startWithLevitation = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "enderDragonGrantsSpin", comment = "Whether you should be able to obtain the spin ability from the ender dragon or take special item." )
	public static Boolean enderDragonGrantsSpin = true;

	@ConfigRange( min = 0, max = 20 )
	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "flightHungerThreshold", comment = "If the player's hunger is below this parameter, he can't open his wings." )
	public static Integer flightHungerThreshold = 6;

	@ConfigRange( min = 0, max = 20 )
	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "flightHungerThreshold", comment = "If the player's hunger is less then or equal to this parameter, the wings will be folded even during flight." )
	public static Integer foldWingsThreshold = 0;

	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "enableFlightFallDamage", comment = "Whether fall damage in flight is included. If true dragon will take damage from the fall." )
	public static Boolean enableFlightFallDamage = true;

	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "lethalFallDamage", comment = "Whether fall damage from flight is lethal, otherwise it will leave you at half a heart" )
	public static Boolean lethalFlight = false;

	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "foldWingsOnLand", comment = "Whether your wings will fold automatically when landing. Has protection against accidental triggering, so the wings do not always close. If False you must close the wings manually." )
	public static Boolean foldWingsOnLand = false;

	@ConfigRange( min = 0, max = 100000 )
	@ConfigOption( side = ConfigSide.SERVER, category = "wings", key = "flightSpinCooldown", comment = "The cooldown in seconds in between uses of the spin attack in flight" )
	public static Integer flightSpinCooldown = 5;

	@ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "stableHover", comment = "Should hovering be completely stable similar to creative flight?")
	public static boolean stableHover = false;

	/**
	 * Sets the fall damage based on flight speed and dragon's size
	 */
	@SubscribeEvent
	public static void changeFallDistance(LivingFallEvent event){
		LivingEntity livingEntity = event.getEntity();
		double flightSpeed = event.getDistance();

		DragonStateProvider.getCap(livingEntity).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon() && dragonStateHandler.hasFlight()){
				try{
					if (!enableFlightFallDamage) {
						event.setCanceled(true);
						return;
					}

					if (flightSpeed <= 2 || dragonStateHandler.isWingsSpread() && !livingEntity.isSprinting() && flightSpeed <= 4) {
						event.setCanceled(true);
						return;
					}

					if (livingEntity.isPassenger() && DragonStateProvider.isDragon(livingEntity.getVehicle())) {
						event.setCanceled(true);
						return;
					}


					MobEffectInstance effectinstance = livingEntity.getEffect(MobEffects.JUMP);
					float f = effectinstance == null ? 0.0F : (float) (effectinstance.getAmplifier() + 1);

					double damage = livingEntity.getDeltaMovement().lengthSqr() * (dragonStateHandler.getSize() / 20);
					damage = Mth.clamp(damage, 0, livingEntity.getHealth() - (lethalFlight ? 0 : 1));

					if (!livingEntity.level().isClientSide() && dragonStateHandler.isWingsSpread()) {
						event.setDistance((float) Math.floor((damage + 3.0F + f) * event.getDamageMultiplier()));
					}
				}finally {
					if(!livingEntity.level().isClientSide()){
						if(foldWingsOnLand){
							if(dragonStateHandler.isWingsSpread()){
								dragonStateHandler.setWingsSpread(false);
								PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SyncFlyingStatus.Data(livingEntity.getId(), false));
							}
						}
					}
				}
			}
		});
	}

	@SubscribeEvent
	public static void foldWings(PlayerTickEvent.Post tickEvent){
		Player player = tickEvent.getEntity();
		if(player.level().isClientSide() || !DragonStateProvider.isDragon(player)){
			return;
		}

		if(!foldWingsOnLand || player.getFoodData().getFoodLevel() <= flightHungerThreshold && !player.isCreative()){
			return;
		}

		DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(player);

		if(dragonStateHandler.isWingsSpread()){
			player.fallDistance = Math.max(0, player.fallDistance * 0.5f);
		}

		if(dragonStateHandler.hasFlown && player.onGround()){
			if(dragonStateHandler.isWingsSpread() && player.isCreative()){
				dragonStateHandler.hasFlown = false;
				dragonStateHandler.setWingsSpread(false);
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncFlyingStatus.Data(player.getId(), false));
			}
		}else{
			if(!dragonStateHandler.hasFlown && isFlying(player)){
				dragonStateHandler.hasFlown = true;
			}
		}
	}

	public static boolean isFlying(LivingEntity player){
		DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(player);
		return dragonStateHandler.hasFlight() && dragonStateHandler.isWingsSpread() && !player.onGround() && !player.isInWater() && !player.isInLava();
	}

	@SubscribeEvent
	public static void playerFlightIcon(PlayerTickEvent.Post playerTickEvent){
		Player player = playerTickEvent.getEntity();
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(handler.isDragon()){
				if(player.tickCount % 10 == 0){
					if(handler.isWingsSpread()){
						if(DragonUtils.isDragonType(player, DragonTypes.SEA)) {
							player.addEffect(new MobEffectInstance(DSEffects.sea_wings, 500, 0, true, false));

						}if(DragonUtils.isDragonType(player, DragonTypes.CAVE)) {
							player.addEffect(new MobEffectInstance(DSEffects.cave_wings, 500, 0, true, false));

						}if(DragonUtils.isDragonType(player, DragonTypes.FOREST)) {
							player.addEffect(new MobEffectInstance(DSEffects.forest_wings, 500, 0, true, false));
						}
					}
				}
			}

			if(!handler.isDragon() || !handler.isWingsSpread()){
				if(player.hasEffect(DSEffects.sea_wings)){
					player.resetFallDistance();
					player.removeEffect(DSEffects.sea_wings);
				}
				if(player.hasEffect(DSEffects.cave_wings)){
					player.resetFallDistance();
					player.removeEffect(DSEffects.cave_wings);
				}
				if(player.hasEffect(DSEffects.forest_wings)){
					player.resetFallDistance();
					player.removeEffect(DSEffects.forest_wings);
				}
			}
		});
	}

	@SubscribeEvent
	public static void playerFlightAttacks(PlayerTickEvent.Post playerTickEvent){

		Player player = playerTickEvent.getEntity();
		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(handler.isDragon()){
				if(handler.getMovementData().spinAttack > 0){
					if(!isFlying(player) && !canSwimSpin(player)){
						if(!player.level().isClientSide()){
							handler.getMovementData().spinAttack = 0;
							PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSpinStatus.Data(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));
						}
					}
				}

				if(isSpin(player)){
					int range = 5;
					List<Entity> entities = player.level().getEntities(null, new AABB(player.position().x - range, player.position().y - range, player.position().z - range, player.position().x + range, player.position().y + range, player.position().z + range));
					entities.removeIf(e -> e.distanceTo(player) > range);
					entities.remove(player);
					entities.removeIf(e -> e instanceof Player && !player.canHarmPlayer((Player)e));
					for(Entity ent : entities){
						if(player.hasPassenger(ent) ){
							continue;
						}
						if(ent instanceof LivingEntity entity){
							//Don't hit the same mob multiple times
							if(entity.getLastHurtByMob() == player && entity.getLastHurtByMobTimestamp() <= entity.tickCount + 5 * 20){
								continue;
							}
						}
						player.attack(ent);
					}

					handler.getMovementData().spinAttack--;

					if(!player.level().isClientSide()){
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSpinStatus.Data(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));
					}
				}else if(handler.getMovementData().spinCooldown > 0){
					if(!player.level().isClientSide()){
						handler.getMovementData().spinCooldown--;
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSpinStatus.Data(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));
					}
				}
			}
		});
	}

	public static boolean isSpin(Player entity){
		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(entity);

		if(isFlying(entity) || canSwimSpin(entity)){
			return handler.getMovementData().spinAttack > 0;
		}

		return false;
	}

	public static boolean canSwimSpin(LivingEntity player){
		DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(player);
		boolean validSwim = (Objects.equals(dragonStateHandler.getType(), DragonTypes.SEA) || Objects.equals(dragonStateHandler.getType(), DragonTypes.FOREST)) && player.isInWater() || player.isInLava() && Objects.equals(dragonStateHandler.getType(), DragonTypes.CAVE);
		return validSwim && dragonStateHandler.hasFlight() && !player.onGround();
	}

	@ConfigRange(min = 1, max = 60 * 60 * 20)
	@ConfigOption(side = ConfigSide.SERVER, key = "flightHungerTicks", category = "wings", comment = "How many ticks it takes for one hunger point to be drained while flying, this is based on hover flight.")
	public static int flightHungerTicks = 50;

	@SubscribeEvent
	public static void playerFoodExhaustion(PlayerTickEvent.Post playerTickEvent){
		Player player = playerTickEvent.getEntity();

		DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				boolean wingsSpread = dragonStateHandler.isWingsSpread();

				if(wingsSpread){
						if(isFlying(player)){
							if(!player.level().isClientSide()){
								if(player.getFoodData().getFoodLevel() <= foldWingsThreshold && !player.isCreative()){
									player.sendSystemMessage(Component.translatable("ds.wings.nohunger"));
									dragonStateHandler.setWingsSpread(false);
									PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncFlyingStatus.Data(player.getId(), false));
									return;
								}
							}
							Vec3 delta = player.getDeltaMovement();
							float moveSpeed = (float)delta.horizontalDistance();
							float l = 4f / flightHungerTicks;
							float moveSpeedReq = 1.0F;
							float minFoodReq = l / 10f;
							float drain = Math.max(minFoodReq, (float)(Math.min(1.0, Math.max(0, Math.max(moveSpeedReq - moveSpeed, 0) / moveSpeedReq)) * l));
							AttributeInstance flightStamina;
							if ((flightStamina = player.getAttribute(DSAttributes.FLIGHT_STAMINA_COST)) != null && flightStamina.getValue() != 0) {
								drain /= (float) flightStamina.getValue();
							}

							player.causeFoodExhaustion(drain);
						}
					}
			}
		});
	}

	public static boolean isGliding(Player player){
		DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(player);
		boolean hasFood = player.getFoodData().getFoodLevel() > flightHungerThreshold || player.isCreative();
		return hasFood && player.isSprinting() && isFlying(player);
	}

	public static double distanceFromGround(Player player){
		BlockPos blockHeight = player.level().getHeightmapPos(Types.MOTION_BLOCKING, player.blockPosition());
		int height = blockHeight.getY();
        return Math.max(0, player.position().y - height);
	}
}