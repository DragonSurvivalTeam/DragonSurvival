package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EntityScale;
import by.dragonsurvivalteam.dragonsurvival.config.OffsetConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonPassengerID;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.text.NumberFormat;
import java.util.List;

@EventBusSubscriber
public class DragonRidingHandler {
    private static final TagKey<EntityType<?>> BOATS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("c", "boats"));

    @Translation(key = "riding_offsets", type = Translation.Type.CONFIGURATION, comments = {
            "Offset the riding position per entity type",
            "Format: resource/tag;x_offset;y_offset;z_offset",
            "The resource can also be defined using regular expressions (for both namespace and path)",
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "riding", key = "riding_offsets")
    public static List<OffsetConfig> OFFSETS = List.of(
            // To avoid touching the water
            OffsetConfig.create(BOATS, new Vec3(0, 0.9, 0))
    );
    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "player_riding_scale", type = Translation.Type.CONFIGURATION, comments = "Maximum human-rider size compared to mount dragon. Default: 0.7 (70%).")
    @ConfigOption(side = ConfigSide.SERVER, category = "riding", key = "player_riding_scale")
    public static Float playerRidingScale = 0.7F;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "dragon_riding_scale", type = Translation.Type.CONFIGURATION, comments = "Maximum dragon-rider size compared to mount dragon. Default: 0.8 (80%).")
    @ConfigOption(side = ConfigSide.SERVER, category = "riding", key = "dragon_riding_scale")
    public static Float dragonRidingScale = 0.8F;

    @Translation(comments = "You are too big to mount on this creature. You must be at most %s the scale of the creature you are trying to ride or smaller, but you are scale %s and the creature is scale %s.")
    private static final String SELF_TOO_BIG = Translation.Type.GUI.wrap("message.self_too_big");

    @Translation(comments = "The creature you are trying to ride must be crouching for you to mount them.")
    private static final String NOT_CROUCHING = Translation.Type.GUI.wrap("message.not_crouching");

    @Translation(comments = "The creature is not rideable.")
    private static final String NOT_RIDEABLE = Translation.Type.GUI.wrap("message.not_rideable");

    public static final String MOUNTING_BONE = "MountingBone";
    public static final int NO_PASSENGER = -1;

    public static final Vec3 BASE_MOUNTING_OFFSET = new Vec3(0, 0.63, 0);
    private static final Vec3 PLAYER_VEHICLE_ATTACHMENT = new Vec3(0, 0.6, 0);
    public static final float PLAYER_RIDING_SCALE_RATIO = playerRidingScale;
    public static final float DRAGON_RIDING_SCALE_RATIO = dragonRidingScale;

    private enum DragonRideAttemptResult {
        SELF_TOO_BIG,
        NOT_CROUCHING,
        NOT_RIDEABLE,
        OTHER,
        SUCCESS
    }

    public static Vec3 getMountingOffsetForEntity(final Entity entity) {
        for (OffsetConfig config : DragonRidingHandler.OFFSETS) {
            //noinspection deprecation -> ignore
            Vec3 offset = config.getOffset(entity.getType().builtInRegistryHolder().key());

            if (offset != null) {
                return offset;
            }
        }

        return BASE_MOUNTING_OFFSET;
    }

    /** Reconstructs the vehicle attachment point introduced after 1.20.1. */
    public static Vec3 getVehicleAttachmentPoint(final Entity entity) {
        if (entity instanceof Player) {
            return PLAYER_VEHICLE_ATTACHMENT;
        }

        return new Vec3(0, -entity.getMyRidingOffset(), 0);
    }

    /** Dragon riders stand on the mounting bone instead of using the seated humanoid attachment point. */
    public static Vec3 getVehicleAttachmentPoint(final Entity rider, final Entity vehicle) {
        if (DragonStateProvider.isDragon(rider) && DragonStateProvider.isDragon(vehicle)) {
            return Vec3.ZERO;
        }

        return getVehicleAttachmentPoint(rider);
    }

    public static boolean dragonIsRideable(final Player player) {
        DragonStateHandler data = DragonStateProvider.getData(player);
        return !data.body().value().noDragonModelRendering() && data.body().value().rideable();
    }

    private static DragonRideAttemptResult playerCanRideDragon(Player rider, Player mount) {
        if (rider.isSpectator() || mount.isSpectator() || rider.isSleeping() || mount.isSleeping()) {
            return DragonRideAttemptResult.OTHER;
        }

        DragonStateHandler mountData = DragonStateProvider.getData(mount);

        if (!mountData.isDragon()) {
            return DragonRideAttemptResult.OTHER;
        }

        double scaleRatio = EntityScale.get(rider) / EntityScale.get(mount);
        boolean dragonIsTooSmallToRide = DragonStateProvider.isDragon(rider) ? scaleRatio >= DRAGON_RIDING_SCALE_RATIO : scaleRatio >= PLAYER_RIDING_SCALE_RATIO;

        if (dragonIsTooSmallToRide) {
            return DragonRideAttemptResult.SELF_TOO_BIG;
        } else if (mount.getPose() != Pose.CROUCHING) {
            return DragonRideAttemptResult.NOT_CROUCHING;
        } else if (!dragonIsRideable(mount)) {
            return DragonRideAttemptResult.NOT_RIDEABLE;
        }

        return DragonRideAttemptResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onRideAttempt(final PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof ServerPlayer target)) {
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getItemStack().isEmpty()) {
            return;
        }

        Player self = event.getEntity();
        DragonRideAttemptResult result = playerCanRideDragon(self, target);

        if (result == DragonRideAttemptResult.SUCCESS && !target.isVehicle()) {
            self.startRiding(target);
            target.connection.send(new ClientboundSetPassengersPacket(target));

            DragonStateProvider.getData(target).setPassengerId(self.getId());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new SyncDragonPassengerID(target.getId(), self.getId()));

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        } else {
            if (result == DragonRideAttemptResult.SELF_TOO_BIG) {
                float ridingScaleRatio = DragonStateProvider.isDragon(self) ? DRAGON_RIDING_SCALE_RATIO : PLAYER_RIDING_SCALE_RATIO;
                self.sendSystemMessage(Component.translatable(SELF_TOO_BIG, NumberFormat.getPercentInstance().format(ridingScaleRatio), String.format("%.2f", EntityScale.get(self)), String.format("%.2f", EntityScale.get(target))));
            } else if (result == DragonRideAttemptResult.NOT_CROUCHING) {
                self.sendSystemMessage(Component.translatable(NOT_CROUCHING));
            } else if (result == DragonRideAttemptResult.NOT_RIDEABLE) {
                self.sendSystemMessage(Component.translatable(NOT_RIDEABLE));
            }
        }
    }

    @SubscribeEvent
    public static void updateRidingState(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
                int passengerId = dragonStateHandler.getPassengerId();
                if (passengerId == NO_PASSENGER) {
                    return;
                }

                Entity passenger = player.level().getEntity(passengerId);
                // Check for any way that riding could have been interrupted and update our internal state tracking
                if (passenger == null || !player.hasPassenger(passenger) || passenger.getRootVehicle() != player.getRootVehicle() || !player.isVehicle()) {
                    dragonStateHandler.setPassengerId(NO_PASSENGER);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonPassengerID(player.getId(), NO_PASSENGER));
                    if (passenger != null) {
                        passenger.stopRiding();
                    }
                    player.connection.send(new ClientboundSetPassengersPacket(player));
                    return;
                }

                if (passenger instanceof Player playerPassenger) {
                    // In addition, if any of the conditions to allow a player to ride a dragon are no longer met, dismount the player
                    DragonRideAttemptResult result = playerCanRideDragon(playerPassenger, player);
                    if (result == DragonRideAttemptResult.SUCCESS || result == DragonRideAttemptResult.NOT_CROUCHING) {
                        return;
                    }

                    dragonStateHandler.setPassengerId(NO_PASSENGER);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonPassengerID(player.getId(), NO_PASSENGER));
                    passenger.stopRiding();
                    player.connection.send(new ClientboundSetPassengersPacket(player));
                }
            });
        }
    }

    @SubscribeEvent
    public static void dismountOnPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getVehicle() instanceof ServerPlayer vehicle) {
            DragonStateProvider.getOptional(vehicle).ifPresent(handler -> {
                player.stopRiding();
                vehicle.connection.send(new ClientboundSetPassengersPacket(vehicle));
                handler.setPassengerId(NO_PASSENGER);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(vehicle, new SyncDragonPassengerID(vehicle.getId(), NO_PASSENGER));
            });
        }
    }
}
