package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class DragonRidingHandler {
    @Translation(type = Translation.Type.MISC, comments = "You are too big to mount on this dragon. You must be at least half the size of the dragon you are trying to ride or smaller, but you are size %s and the dragon is size %s.")
    private static final String SELF_TOO_BIG = Translation.Type.GUI.wrap("message.self_too_big");

    @Translation(type = Translation.Type.MISC, comments = "The dragon you are riding is too young. To ride a dragon as a human, the dragon must be an adult.")
    private static final String TARGET_TOO_SMALL = Translation.Type.GUI.wrap("message.target_too_small");

    @Translation(type = Translation.Type.MISC, comments = "The dragon you are trying to ride must be crouching for you to mount them.")
    private static final String NOT_CROUCHING = Translation.Type.GUI.wrap("message.not_crouching");

    public static final int NO_PASSENGER = -1;

    /** This is just the default adult dragon minimum size */
    private static final int PLAYER_RIDING_SIZE = 30;

    private enum DragonRideAttemptResult {
        SELF_TOO_BIG,
        MOUNT_TOO_SMALL_HUMAN,
        NOT_CROUCHING,
        OTHER,
        SUCCESS
    }

    private static DragonRideAttemptResult playerCanRideDragon(Player rider, Player mount) {
        if (rider.isSpectator() || mount.isSpectator() || rider.isSleeping() || mount.isSleeping()) {
            return DragonRideAttemptResult.OTHER;
        }

        DragonStateHandler mountData = DragonStateProvider.getData(mount);

        if (!mountData.isDragon()) {
            return DragonRideAttemptResult.OTHER;
        }

        DragonStateHandler riderData = DragonStateProvider.getData(rider);

        double sizeRatio = riderData.getSize() / mountData.getSize();
        boolean dragonIsTooSmallToRide = sizeRatio >= 0.5;

        if (dragonIsTooSmallToRide) {
            return DragonRideAttemptResult.SELF_TOO_BIG;
        } else if (!riderData.isDragon() && mountData.getSize() < PLAYER_RIDING_SIZE) {
            return DragonRideAttemptResult.MOUNT_TOO_SMALL_HUMAN;
        } else if (mount.getPose() != Pose.CROUCHING) {
            return DragonRideAttemptResult.NOT_CROUCHING;
        }

        return DragonRideAttemptResult.SUCCESS;
    }

    /** Mounting a dragon */
    @SubscribeEvent
    public static void onRideAttempt(PlayerInteractEvent.EntityInteractSpecific event) { // TODO :: check
        Entity entity = event.getTarget();

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (entity instanceof ServerPlayer target) {
            Player self = event.getEntity();

            DragonRideAttemptResult result = playerCanRideDragon(self, target);

            if (result == DragonRideAttemptResult.SUCCESS && !target.isVehicle()) {
                self.startRiding(target);
                target.connection.send(new ClientboundSetPassengersPacket(target));

                DragonStateProvider.getData(target).setPassengerId(self.getId());
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new SyncDragonPassengerID.Data(target.getId(), self.getId()));

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            } else {
                if (result == DragonRideAttemptResult.MOUNT_TOO_SMALL_HUMAN) {
                    self.sendSystemMessage(Component.translatable(TARGET_TOO_SMALL));
                } else if (result == DragonRideAttemptResult.SELF_TOO_BIG) {
                    DragonStateHandler targetData = DragonStateProvider.getData(target);
                    DragonStateHandler selfData = DragonStateProvider.getData(self);

                    self.sendSystemMessage(Component.translatable(SELF_TOO_BIG, String.format("%.0f", selfData.getSize()), String.format("%.0f", targetData.getSize())));
                } else if (result == DragonRideAttemptResult.NOT_CROUCHING) {
                    self.sendSystemMessage(Component.translatable(NOT_CROUCHING));
                }
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
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonPassengerID.Data(player.getId(), NO_PASSENGER));
                    return;
                }

                if (passenger instanceof Player playerPassenger) {
                    // In addition, if any of the conditions to allow a player to ride a dragon are no longer met, dismount the player
                    DragonRideAttemptResult result = playerCanRideDragon(playerPassenger, player);
                    if (result == DragonRideAttemptResult.SUCCESS || result == DragonRideAttemptResult.NOT_CROUCHING) {
                        return;
                    }

                    dragonStateHandler.setPassengerId(NO_PASSENGER);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonPassengerID.Data(player.getId(), NO_PASSENGER));
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
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(vehicle, new SyncDragonPassengerID.Data(vehicle.getId(), NO_PASSENGER));
            });
        }
    }
}