package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinDurationAndCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingsSpread;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Used in pair with {@link ClientFlightHandler} */
@EventBusSubscriber
public class ServerFlightHandler {
    public static final int SPIN_DURATION = Functions.secondsToTicks(0.76);

    @ConfigRange(min = 0.1, max = 1)
    @Translation(key = "flight_speed_multiplier", type = Translation.Type.CONFIGURATION, comments = "Flight speed multiplier")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "flight_speed_multiplier")
    public static Double maxFlightSpeed = 0.3;

    @Translation(key = "start_with_flight", type = Translation.Type.CONFIGURATION, comments = {
            "If enabled dragons can fly from the start",
            "If disabled players will have to use the item that grants wings or interact with the ender dragon"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "start_with_flight")
    public static Boolean startWithFlight = true;

    @ConfigRange(min = 0, max = 20)
    @Translation(key = "flight_hunger_threshold", type = Translation.Type.CONFIGURATION, comments = "Determines the required food values to be able to fly")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "flight_hunger_threshold")
    public static Integer flightHungerThreshold = 6;

    @ConfigRange(min = 0, max = 20)
    @Translation(key = "fold_wings_threshold", type = Translation.Type.CONFIGURATION, comments = "Determines the food values at which the dragon will stop being able to fly mid-flight")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "fold_wings_threshold")
    public static Integer foldWingsThreshold = 0;

    @Translation(key = "fold_wings_on_land", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will automatically stop fold their wings (i.e. stop flying) when landing")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "fold_wings_on_land")
    public static Boolean foldWingsOnLand = false;

    @ConfigRange(min = 0, max = /* 1 hour */ 3600)
    @Translation(key = "flight_spin_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown (in seconds) of the spin attack during flifght")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "flight_spin_cooldown")
    public static Integer flightSpinCooldown = 5;

    @ConfigRange(min = 1, max = /* 1 hour */ 72_000)
    @Translation(key = "flight_hunger_ticks", type = Translation.Type.CONFIGURATION, comments = "Determines the amount of ticks (20 ticks = 1 second) it takes for one hunger point to be drained while flying")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "flight_hunger_ticks")
    public static int flightHungerTicks = 50;

    @Translation(key = "stable_hover", type = Translation.Type.CONFIGURATION, comments = "If enabled hovering will behave the same as creative flight (i.e. stable flight)")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "stable_hover")
    public static boolean stableHover = false;

    @Translation(key = "collision_damage_speed_factor", type = Translation.Type.CONFIGURATION, comments = "How much does the change in horizontal speed impact the damage taken from a collision whilst flying?")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "collision_damage_speed_factor")
    public static float collisionDamageSpeedFactor = 10.0f;

    @Translation(key = "collision_damage_threshold", type = Translation.Type.CONFIGURATION, comments = "The amount of damage subtracted from the base damage when a collision occurs whilst flying.")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "collision_damage_threshold")
    public static float collisionDamageThreshold = 3.0f;

    @Translation(key = "enable_collision_damage", type = Translation.Type.CONFIGURATION, comments = "Dragons will take damage from colliding whilst glide-flying (similar to elytra).")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "enable_collision_damage")
    public static boolean enableCollisionDamage = true;

    @Translation(key = "enable_flight_fall_damage", type = Translation.Type.CONFIGURATION, comments = "Dragons will take fall damage from colliding whilst glide-flying (similar to elytra).")
    @ConfigOption(side = ConfigSide.SERVER, category = "wings", key = "enable_flight_fall_damage")
    public static boolean enableFlightFallDamage = true;

    @SubscribeEvent
    public static void foldWingsOnLand(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();

        if (livingEntity.level().isClientSide()) {
            return;
        }

        DragonStateProvider.getOptional(livingEntity).ifPresent(handler -> {
            if (!foldWingsOnLand || !handler.isDragon()) {
                return;
            }

            if (!(livingEntity instanceof Player player)) {
                return;
            }

            FlightData data = FlightData.getData(player);

            if (data.isWingsSpread()) {
                FlightData.getData(player).areWingsSpread = false;
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new SyncWingsSpread.Data(livingEntity.getId(), false));
            }
        });
    }

    /**
     * Sets the fall damage based on flight speed and dragon's size
     */
    @SubscribeEvent
    public static void changeFallDistanceToUseFlightSpeed(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        double verticalFlightSpeed = livingEntity.getDeltaMovement().y * livingEntity.getDeltaMovement().y;

        if(livingEntity instanceof Player player) {
            DragonStateProvider.getOptional(livingEntity).ifPresent(handler -> {
                FlightData data = FlightData.getData(player);
                // Don't use the helper functions here, as isFlying() will return false if the player is grounded
                if (handler.isDragon() && data.hasFlight() && data.isWingsSpread() && player.isSprinting()) {
                    if (!enableFlightFallDamage
                            || verticalFlightSpeed <= 1
                            || (livingEntity.isPassenger() && DragonStateProvider.isDragon(livingEntity.getVehicle())))
                    {
                        event.setCanceled(true);
                        return;
                    }

                    double damage = verticalFlightSpeed * (handler.getSize() / 20);
                    damage = Mth.clamp(damage, 0, livingEntity.getHealth() -  1);

                    // See Attributes.SAFE_FALL_DISTANCE
                    final float SAFE_FALL_DISTANCE_DEFAULT_VALUE = 3.0f;
                    event.setDistance((float) damage + SAFE_FALL_DISTANCE_DEFAULT_VALUE);
                }
            });
        }
    }

    public static boolean isFlying(Player player) {
        FlightData data = FlightData.getData(player);
        return data.hasFlight() && data.isWingsSpread() && !player.onGround() && !player.isInWater() && !player.isInLava();
    }

    @SubscribeEvent
    public static void handleEarlyFlightLogic(final PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        // Handle this on both the client and server so that the fall damage sound effects are correctly handled
        if (isGliding(player)) {
            // Gather collision data
            handler.preCollisionDeltaMovement = player.getDeltaMovement();
        } else if (isFlying(player)) {
            // Handle fall distance
            player.resetFallDistance();
        }
    }

    @SubscribeEvent
    public static void handleWallCollisionsWhenFlying(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        // This collision code is from Elytra flying. See isFallFlying() section in LivingEntity#travel()
        if (player.horizontalCollision && isGliding(player) && enableCollisionDamage) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            double lostSpeed = handler.preCollisionDeltaMovement.horizontalDistance() - player.getDeltaMovement().horizontalDistance();
            float damage = (float) (lostSpeed * collisionDamageSpeedFactor - collisionDamageThreshold);

            if (damage > 0) {
                player.playSound(player.getFallDamageSound((int) damage), 1, 1);
                player.hurt(player.damageSources().flyIntoWall(), damage);
                FlightData.getData(player).areWingsSpread = false;
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread.Data(player.getId(), false));
            }
        }
    }

    @SubscribeEvent
    public static void playerFlightAttacks(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        // TODO: Some strange choices on what is run clientside here. Are we sure this is safe?
        FlightData spin = FlightData.getData(player);
        if (!player.level().isClientSide()) {
            if(spin.duration > 0) {
                if (!isFlying(player) && !canSwimSpin(player)) {
                    spin.duration = 0;
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SpinDurationAndCooldown(player.getId(), spin.duration, spin.cooldown));
                }
            }
        }

        if (isSpin(player)) {
            int range = 5;
            List<Entity> entities = player.level().getEntities(null, new AABB(player.position().x - range, player.position().y - range, player.position().z - range, player.position().x + range, player.position().y + range, player.position().z + range));

            for (Entity target : entities) {
                if (target == player) {
                    continue;
                }

                if (target instanceof Player otherPlayer && !player.canHarmPlayer(otherPlayer)) {
                    continue;
                }

                if (target.distanceTo(player) > range) {
                    continue;
                }

                if (player.hasPassenger(target)) {
                    continue;
                }

                if (target instanceof LivingEntity entity) {
                    // Don't hit the same mob multiple times
                    if (entity.getLastHurtByMob() == player && entity.getLastHurtByMobTimestamp() <= entity.tickCount + 5 * 20) {
                        continue;
                    }
                }

                player.attack(target);
            }

            spin.duration--;

            if (!player.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SpinDurationAndCooldown(player.getId(), spin.duration, spin.cooldown));
            }
        } else if (spin.cooldown > 0 && !player.level().isClientSide()) {
            spin.cooldown--;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SpinDurationAndCooldown(player.getId(), spin.duration, spin.cooldown));
        }
    }

    public static boolean isSpin(Player entity) {
        FlightData spin = FlightData.getData(entity);
        if (isFlying(entity) || canSwimSpin(entity)) {
            return spin.duration > 0;
        }

        return false;
    }

    public static boolean canSwimSpin(Player player){
        FlightData data = FlightData.getData(player);
        return data.swimSpinFluid.isPresent() && player.isEyeInFluidType(data.swimSpinFluid.get().value()) && data.hasFlight() && !player.onGround();
    }

    @SubscribeEvent
    public static void playerFoodExhaustion(PlayerTickEvent.Post playerTickEvent) {
        Player player = playerTickEvent.getEntity();

        DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
            if (dragonStateHandler.isDragon()) {
                boolean wingsSpread = FlightData.getData(player).isWingsSpread();

                if (wingsSpread) {
                    if (isFlying(player)) {
                        if (!player.level().isClientSide()) {
                            if (player.getFoodData().getFoodLevel() <= foldWingsThreshold && !player.isCreative()) {
                                player.sendSystemMessage(Component.translatable(LangKey.MESSAGE_NO_HUNGER));
                                FlightData.getData(player).areWingsSpread = false;
                                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread.Data(player.getId(), false));
                                return;
                            }
                        }
                        Vec3 delta = player.getDeltaMovement();
                        float moveSpeed = (float) delta.horizontalDistance();
                        float l = 4f / flightHungerTicks;
                        float moveSpeedReq = 1.0F;
                        float minFoodReq = l / 10f;
                        float drain = Math.max(minFoodReq, (float) (Math.min(1.0, Math.max(0, Math.max(moveSpeedReq - moveSpeed, 0) / moveSpeedReq)) * l));

                        double flightStamina = player.getAttributeValue(DSAttributes.FLIGHT_STAMINA_COST);

                        if (flightStamina > 0) {
                            drain /= (float) flightStamina;
                        }

                        player.causeFoodExhaustion(drain);
                    }
                }
            }
        });
    }

    public static boolean isGliding(Player player) {
        boolean hasFood = player.getFoodData().getFoodLevel() > flightHungerThreshold || player.isCreative();
        return hasFood && player.isSprinting() && isFlying(player) && !player.hasEffect(MobEffects.LEVITATION);
    }

    public static double distanceFromGround(Player player) {
        BlockPos blockHeight = player.level().getHeightmapPos(Types.MOTION_BLOCKING, player.blockPosition());
        int height = blockHeight.getY();
        return Math.max(0, player.position().y - height);
    }
}