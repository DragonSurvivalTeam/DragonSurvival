package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.OffsetConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonPassengerID;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.List;

@EventBusSubscriber
public class DragonRidingHandler {
    @Translation(key = "riding_offsets", type = Translation.Type.CONFIGURATION, comments = {
            "Offset the riding position per entity type",
            "Format: resource/tag;x_offset;y_offset;z_offset",
            "The resource can also be defined using regular expressions (for both namespace and path)",
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "riding", key = "riding_offsets")
    public static List<OffsetConfig> OFFSETS = List.of(
            // To avoid touching the water
            OffsetConfig.create(Tags.EntityTypes.BOATS, new Vec3(0, 0.9, 0))
    );

    @Translation(comments = "You are too big to mount on this dragon. You must be at most %s the scale of the dragon you are trying to ride or smaller, but you are scale %s and the dragon is scale %s.")
    private static final String SELF_TOO_BIG = Translation.Type.GUI.wrap("message.self_too_big");

    @Translation(comments = "The dragon you are trying to ride must be crouching for you to mount them.")
    private static final String NOT_CROUCHING = Translation.Type.GUI.wrap("message.not_crouching");

    public static final int NO_PASSENGER = -1;

    public static final Vec3 BASE_MOUNTING_OFFSET = new Vec3(0, 0.63, 0);
    public static final float PLAYER_RIDING_SCALE_RATIO = 0.8F;
    public static final float DRAGON_RIDING_SCALE_RATIO = 0.5F;

    private enum DragonRideAttemptResult {
        SELF_TOO_BIG,
        NOT_CROUCHING,
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

    private static DragonRideAttemptResult playerCanRideDragon(Player rider, Player mount) {
        if (rider.isSpectator() || mount.isSpectator() || rider.isSleeping() || mount.isSleeping()) {
            return DragonRideAttemptResult.OTHER;
        }

        DragonStateHandler mountData = DragonStateProvider.getData(mount);

        if (!mountData.isDragon() || mountData.body().value().mountingOffsets().isEmpty()) {
            return DragonRideAttemptResult.OTHER;
        }

        double scaleRatio = rider.getScale() / mount.getScale();
        boolean dragonIsTooSmallToRide = DragonStateProvider.isDragon(rider) ? scaleRatio >= DRAGON_RIDING_SCALE_RATIO : scaleRatio >= PLAYER_RIDING_SCALE_RATIO;

        if (dragonIsTooSmallToRide) {
            return DragonRideAttemptResult.SELF_TOO_BIG;
        } else if (mount.getPose() != Pose.CROUCHING) {
            return DragonRideAttemptResult.NOT_CROUCHING;
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
                self.sendSystemMessage(Component.translatable(SELF_TOO_BIG, NumberFormat.getPercentInstance().format(ridingScaleRatio), String.format("%.2f", self.getScale()), String.format("%.2f", target.getScale())));
            } else if (result == DragonRideAttemptResult.NOT_CROUCHING) {
                self.sendSystemMessage(Component.translatable(NOT_CROUCHING));
            }
        }
    }

    @SubscribeEvent
    public static void updateRidingState(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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
                    if(passenger != null) {
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