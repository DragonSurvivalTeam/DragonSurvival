package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class PlayerLoginHandler {

    public static void syncCompleteSingle(Entity tracker, Entity tracked) {
        if (tracker instanceof ServerPlayer) {
            if (tracked instanceof ServerPlayer) {
                DragonStateProvider.getOptional(tracked).ifPresent(dragonStateHandler -> {
                    PacketDistributor.sendToPlayer((ServerPlayer) tracker, new SyncComplete.Data(tracked.getId(), dragonStateHandler.serializeNBT(tracked.registryAccess())));
                });
            }
        }
    }

    public static void syncCompleteSingle(final Entity entity) {
        if (entity instanceof ServerPlayer player) {
            DragonStateProvider.getOptional(player).ifPresent(handler -> {
                if (handler.getType() != null && handler.getBody() == null) {
                    // Otherwise players won't be able to join the world
                    handler.setBody(player, DragonBody.random(player.registryAccess()));
                    DragonSurvival.LOGGER.error("Player {} was a dragon but had an invalid dragon body type", player);
                }

                SyncComplete.handleDragonSync(player);
                PacketDistributor.sendToPlayer(player, new SyncComplete.Data(player.getId(), handler.serializeNBT(player.registryAccess())));
            });

            MagicData magicData = MagicData.getData(player);
            PacketDistributor.sendToPlayer(player, new SyncMagicData.Data(player.getId(), magicData.serializeNBT(player.registryAccess())));

            ModifiersWithDuration modifiers = player.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);
            modifiers.syncModifiersToPlayer(player);

            PenaltySupply penaltySupply = player.getData(DSDataAttachments.PENALTY_SUPPLY);
            penaltySupply.syncPenaltySupplyToPlayer(player);
        }
    }

    public static void syncCompleteAll(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
                SyncComplete.handleDragonSync(player);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncComplete.Data(player.getId(), dragonStateHandler.serializeNBT(player.registryAccess())));
            });
        }
    }

    public static void stopTickingSounds(final Entity tracker, final Entity tracked) {
        if (tracker instanceof ServerPlayer trackerPlayer && tracked instanceof ServerPlayer trackedPlayer) {
            MagicData magicDataTracked = MagicData.getData(trackedPlayer);
            DragonAbilityInstance currentlyCasting = magicDataTracked.getCurrentlyCasting();

            if (currentlyCasting != null) {
                PacketDistributor.sendToPlayer(trackerPlayer, new StopTickingSound(currentlyCasting.location().withSuffix(trackedPlayer.getStringUUID())));
            }
        }
    }

    // TODO: Do we need to start up any existing ticking sounds when a player starts getting tracked? e.g. moves into render distance while casting.
    // Do we even care enough to account for this edge case?
    @SubscribeEvent
    public static void onTrackingStart(final PlayerEvent.StartTracking event) {
        Entity tracker = event.getEntity();
        Entity tracked = event.getTarget();
        syncCompleteSingle(tracker, tracked);
    }

    @SubscribeEvent
    public static void onTrackingEnd(final PlayerEvent.StopTracking event) {
        Entity tracker = event.getEntity();
        Entity tracked = event.getTarget();
        stopTickingSounds(tracker, tracked);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncCompleteSingle(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
           syncCompleteSingle(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncCompleteSingle(event.getEntity());
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

        PacketDistributor.sendToPlayer(serverPlayer, OpenDragonAltar.INSTANCE);
        data.isInAltar = true;
    }
}
