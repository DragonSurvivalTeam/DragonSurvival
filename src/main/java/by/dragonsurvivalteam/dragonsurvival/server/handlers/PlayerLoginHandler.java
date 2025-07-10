package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.animation.StopAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class PlayerLoginHandler {
    // TODO: Do we need to start up any existing ticking sounds when a player starts getting tracked? e.g. moves into render distance while casting.
    //  Do we even care enough to account for this edge case?
    @SubscribeEvent
    public static void onTrackingStart(final PlayerEvent.StartTracking event) {
        Player tracker = event.getEntity();
        Entity tracked = event.getTarget();
        syncHandler(tracker, tracked);
    }

    @SubscribeEvent
    public static void onTrackingEnd(final PlayerEvent.StopTracking event) {
        Player tracker = event.getEntity();
        Entity tracked = event.getTarget();
        stopTickingSounds(tracker, tracked);
    }

    // This needs to happen before the call to cancel casting on MagicData, otherwise the sound will not be stopped
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDeath(final LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            stopTickingSoundsForAllPlayers(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            // Is only fired server-side, check is just to have a safe cast
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        // Remove any existing penalty supplies that may no longer be relevant (due to datapack changes)
        player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> {
            if (!handler.isDragon()) {
                // In case the species was removed
                player.removeData(DSDataAttachments.PENALTY_SUPPLY);
                return;
            }

            for (ResourceLocation supplyType : data.getSupplyTypes()) {
                if (handler.species().value().penalties().stream().noneMatch(penalty -> penalty.value().trigger() instanceof SupplyTrigger supplyTrigger && supplyTrigger.supplyType().equals(supplyType))) {
                    data.remove(supplyType);
                }
            }
        });

        if (ServerConfig.noHumansAllowed && !handler.isDragon()) {
            handler.setSpecies(player, DragonSpecies.getRandom(player));
            handler.setBody(player, DragonBody.getRandomUnlocked(player));
            handler.setGrowth(player, handler.species().value().getStartingGrowth(player.registryAccess()));
        }

        syncComplete(player);
    }

    @SubscribeEvent
    public static void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new StopAbilityAnimation(player.getId()));
            syncComplete(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncComplete(event.getEntity());
    }

    @SubscribeEvent
    public static void startWithDragonChoice(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer) || serverPlayer.isDeadOrDying()) {
            return;
        }

        AltarData data = AltarData.getData(serverPlayer);

        if (data.altarCooldown > 0) {
            data.altarCooldown--;
        }

        if (!ServerConfig.startWithDragonChoice || data.hasUsedAltar || data.isInAltar || serverPlayer.tickCount < Functions.secondsToTicks(5)) {
            return;
        }

        PacketDistributor.sendToPlayer(serverPlayer, new OpenDragonAltar(DragonSpecies.getSpecies(serverPlayer, true)));
        data.isInAltar = true;
    }

    private static void stopTickingSounds(final Entity tracker, final Entity tracked) {
        if (tracker instanceof ServerPlayer trackerPlayer && tracked instanceof ServerPlayer trackedPlayer) {
            MagicData magicDataTracked = MagicData.getData(trackedPlayer);
            DragonAbilityInstance currentlyCasting = magicDataTracked.getCurrentlyCasting();

            if (currentlyCasting != null) {
                PacketDistributor.sendToPlayer(trackerPlayer, new StopTickingSound(currentlyCasting.location().withSuffix(trackedPlayer.getStringUUID())));
            }
        }
    }

    private static void stopTickingSoundsForAllPlayers(final ServerPlayer player) {
        MagicData magicData = MagicData.getData(player);
        DragonAbilityInstance currentlyCasting = magicData.getCurrentlyCasting();

        if (currentlyCasting != null) {
            PacketDistributor.sendToAllPlayers(new StopTickingSound(currentlyCasting.location().withSuffix(player.getStringUUID())));
        }
    }

    /** Synchronizes the dragon data to the player and all tracking players */
    public static void syncHandler(final ServerPlayer serverPlayer) {
        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncComplete(serverPlayer.getId(), handler.serializeNBT(serverPlayer.registryAccess())));

        serverPlayer.getExistingData(DSDataAttachments.FLIGHT).ifPresent(data ->
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncData(serverPlayer.getId(), DSDataAttachments.FLIGHT.getId(), data.serializeNBT(serverPlayer.registryAccess())))
        );
    }

    /** Synchronizes the dragon data of the newly tracked player to the tracking player */
    public static void syncHandler(final Player syncTo, final Entity syncFrom) {
        if (syncTo instanceof ServerPlayer target && syncFrom instanceof ServerPlayer source) {
            DragonStateHandler handler = DragonStateProvider.getData(source);
            PacketDistributor.sendToPlayer(target, new SyncComplete(source.getId(), handler.serializeNBT(source.registryAccess())));

            // Make sure to sync the FLIGHT data, otherwise the flight animation will be displayed incorrectly when tracking begins
            syncFrom.getExistingData(DSDataAttachments.FLIGHT).ifPresent(data -> data.sync(source, target));
        }
    }

    /**
     * Synchronizes: <br>
     * - dragon data <br>
     * - magic data (through {@link DragonStateHandler#refreshMagicData}) <br>
     * - other data attachments that may be relevant client-side
     */
    public static void syncComplete(final Entity entity) {
        if (entity instanceof ServerPlayer player) {
            DragonStateProvider.getOptional(player).ifPresent(handler -> {
                SyncComplete.handleDragonSync(player, true);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncComplete(player.getId(), handler.serializeNBT(player.registryAccess())));
            });

            syncDataAttachments(player);
        }
    }

    private static void syncDataAttachments(final ServerPlayer player) {
        player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.ALTAR).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.CLAW_INVENTORY).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.FLIGHT).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.SWIM).ifPresent(data -> data.sync(player));
        DSDataAttachments.getStorages(player).forEach(storage -> storage.sync(player));
    }
}
